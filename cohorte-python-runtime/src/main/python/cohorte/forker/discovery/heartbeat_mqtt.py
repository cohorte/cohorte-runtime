#!/usr/bin/env python
# -- Content-Encoding: UTF-8 --
"""
Forker MQTT heartbeat.

:author: Thomas Calmant
:license: GPLv3
"""

# Documentation strings format
__docformat__ = "restructuredtext en"

# Boot module version
__version__ = "1.0.0"

# ------------------------------------------------------------------------------

# COHORTE constants
import cohorte.forker

# MQTT Client
import pelix.misc.mqtt_client as mqtt

# Pelix/iPOPO
from pelix.ipopo.decorators import ComponentFactory, Requires, Validate, \
    Invalidate, Property
from pelix.utilities import to_unicode
import pelix.http

# ------------------------------------------------------------------------------

# Standard library
import json
import logging

# ------------------------------------------------------------------------------

_logger = logging.getLogger(__name__)

# ------------------------------------------------------------------------------

@ComponentFactory("cohorte-forker-heartbeat-mqtt-factory")
@Property('_server', 'mqtt.host', "localhost")
@Property('_port', 'mqtt.port', 1883)
@Property('_topic_pattern', 'mqtt.topic.pattern', 'cohorte/{appid}/heartbeat')
# To have the same life cycle than the forker...
@Requires("_forker", cohorte.SERVICE_FORKER)
@Requires("_discovery", cohorte.forker.SERVICE_DISCOVERY)
@Requires("_http", pelix.http.HTTP_SERVICE)
class MqttHeartbeat(object):
    """
    The heart beat sender
    """
    def __init__(self):
        """
        Constructor
        """
        # Injected properties
        self._server = ""
        self._port = 0
        self._topic_pattern = ""
        self.__topic = ""

        # Injected services
        self._forker = None
        self._discovery = None
        self._http = None

        # Bundle context
        self._context = None
        self._local_uid = None

        # MQTT client
        self._mqtt = None


    @Validate
    def validate(self, context):
        """
        Component validated
        """
        # Store local properties
        self._context = context
        self._local_uid = self._discovery.get_uid()

        # Normalize properties
        self._port = int(self._port)
        self.__topic = self._topic_pattern \
            .format(appid=self._discovery.get_appid()) \
            .replace('//', '/')
        if self.__topic[-1] == '/':
            # Remove trailing slash
            self.__topic = self.__topic[:-1]

        _logger.info("MQTT Heartbeat validated: server=[%s:%d], topic='%s'",
                     self._server, self._port, self.__topic)

        # Connect to the MQTT server
        self._mqtt = mqtt.MqttClient(mqtt.MqttClient.generate_id("heart-"))
        self._mqtt.on_connect = self.__on_connect
        self._mqtt.on_message = self.__on_message
        # will: removes the retained message
        self._mqtt.set_will(self._make_topic(), "", retain=True)
        self._mqtt.connect(self._server, self._port)


    @Invalidate
    def invalidate(self, context):
        """
        Component invalidated
        """
        # Disconnect from the MQTT server
        self._mqtt.disconnect()
        self._mqtt = None

        _logger.info("MQTT Heartbeat invalidated")

        # Clean up
        self._context = None
        self._local_uid = None
        self.__topic = None


    def _make_topic(self, suffix=None):
        """
        Prepares a topic, based on the configured pattern. If the suffix is not
        given, the local forker UID will be used instead

        :param suffix: Base topic suffix
        :return: An MQTT topic
        """
        return "{0}/{1}".format(self.__topic, suffix or self._local_uid)


    def __on_connect(self, client, result_code):
        """
        MQTT client connected

        :param client: Connected client
        :param result_code: Connection result code (0 for success)
        """
        if result_code == 0:
            # Connected
            _logger.info("MQTT heartbeat connected to the server.")
            client.subscribe(self._make_topic('+'))

            # Prepare the payload
            host, port = self._http.get_access()
            node_uid = self._context.get_property(cohorte.PROP_NODE_UID)
            node_name = self._context.get_property(cohorte.PROP_NODE_NAME)

            payload = {"application_id": self._discovery.get_appid(),
                       "forker_uid": self._local_uid,
                       "node_uid": node_uid,
                       "node_name":  node_name,
                       "host": host,
                       "port": port,
                      }

            # Publish a retained message
            client.publish(self._make_topic(), json.dumps(payload), retain=True)


    def __on_message(self, client, msg):
        """
        MQTT message received

        :param client: Connected client
        :param msg: Message description bean
        """
        # Extract the node UID
        forker_uid = msg.topic.split('/')[-1]
        if forker_uid == self._local_uid:
            # Loopback message: ignore
            return

        data = msg.payload
        if not data:
            # Empty message: client lost
            self._discovery.forker_lost(forker_uid)

        else:
            # Parse payload
            payload = json.loads(to_unicode(data))

            # Register the forker
            self._discovery.register_forker(payload['forker_uid'],
                                            payload['node_uid'],
                                            payload['node_name'],
                                            payload['host'],
                                            payload['port'])


    def __on_disconnect(self, client, result_code):
        """
        MQTT client disconnected

        :param client: Disconnected client
        :param result_code: Disconnection reason (0 for excepted)
        """
        _logger.info("MQTT heartbeat disconnected (reason: %s)", result_code)
