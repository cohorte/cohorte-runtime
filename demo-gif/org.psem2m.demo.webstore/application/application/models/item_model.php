<?php

class Item_model extends CI_Model {

    private $DataServerPort = 9210;

    /**
     *
     * PHP5 constructor
     */
    function __construct(){
        parent::__construct();


        log_message('debug', "** Item_model.[init] [".$this->DataServerPort."]");

        $this->load->library('jsonrpc');
    }

    /**
     * return the found Item bean (array)
     *
     * The returned bean (array) contains five values :
     * - String  'id'
     * - String  'name'
     * - String  'description'
     * - Integer 'price'
     * - Integer 'qualityLevel'
     *
     * @param String $aItemId The item id to find
     * @return Array|NULL  an item bean (array) or null
     */
    public function getItem($aItemId){

        $wResponse = $this->rpcGetItem($aItemId);
        if (!is_null($wResponse)){
                    return  $wResponse;
        }
        return  $this->localGetItem($aItemId);
    }


    /**
     * return an array of item beans (array)
     *
     * Each returned bean (array) contains five values :
     * - String  'id'
     * - String  'name'
     * - String  'description'
     * - Integer 'price'
     * - Integer 'qualityLevel'
     *
     * @param String $aCategorie  The category of the returned items.
     * @param Integer $aNbItems the number of wanted items
     * @param Boolean $aRandom  Gets the items at random in the golbal list of items.
     * @param Integer $aStartIdx The base . This parameter isn't taken if the "aRandom" one is set to true
     */
    public function getItems($aCategory='', $aNbItems=99, $aRandom=false,$aBaseId=''){

        $wResponse = $this->rpcGetItems($aCategory, $aNbItems ,$aRandom, $aBaseId);
        if (!is_null($wResponse)){
            return $wResponse;
        }
        return $this->localGetItems($aCategory, $aNbItems, $aRandom, $aBaseId);
    }

    /**
     *
     * return a array of item beans containing a quantity according the passed list of item ids.
     *
     * Each returned bean (array) contains three values :
     * - String  'id'
     * - Integer 'stock"
     * - Integer 'qualityLevel'
     *
     * 0 : SYNC Top qualité (retour direct de l’ERP)
     * 1 : FRESH Cache niveau 1 (moins de 1 minute (incluses) depuis la mise en cache)
     * 2 : ACCEPTABLE Cache niveau 2 (moins de 5 minutes (incluses) depuis la mise en cache)
     * 3 : WARNING Cache niveau 3 (moins de 15 minutes (incluses) depuis la mise en cache)
     * 4 : CRITICAL Qualité critique (plus de 15 minutes depuis la mise en cache)
     *
     * @param Array $aItemIds An array of item ids.
     */
    public function getItemsStock($aItemIds){

        $wResponse =  $this->rpcGetItemsStock($aItemIds);
        if (!is_null($wResponse)){
                return $wResponse;
        }
        return  $this->localGetItemsStock($aItemIds);
    }

    /* ************************************************************************************************
     *
    * RPC
    *
    * Approximately conforms to the JSON-RPC 1.1 working draft.
    * Because it's a working draft, there are certain aspects to it that are unfinished but,
    * as a whole it is a significant improvement in quality over JSON-RPC 1.0.
    * The server *should* be backwards-compatible with JSON-RPC 1.0, although this is untested.
    *
    * Jsonrpc server comes in two parts, a client and a server.
    * The client is used to request JSON information from remote sources, the serve is used to
    * serve (mostly-)valid JSON-RPC content to requesting resources.
    * The client supports requesting data via both GET and POST, while the server only responds
    * to POST requests for the time being.
    *
    * Using the JSON-RPC library:
    * To use the library, load it in CodeIgniter.
    * This can be done with $this->load->library('jsonrpc'). From there, you can access the
    * client or server functionality with $this->jsonrpc->get_client() and
    * $this->jsonrpc->get_server().
    *
    * Both the client and the server were modeled of of CodeIgniter's included XML-RPC
    * libraries, although there are certain differences.
    *
    * ************************************************************************************************/

    /**
     * retrieve an Item asking the dataserver
     * <pre>
     * >>> pprint(s.dataserver.getItem("screen001"))
     * {u'javaClass': u'java.util.HashMap',
     *  u'map': {u'description': u'21.5" (54.6 cm) - Full HD (1920x1080) - 5 ms - 250 cd/m - DVI-D\n\t\t\t/ VGA / Audio\n\t\t\tMoniteur Asus VE228T. Profitez d\'images plus ralistes\n\t\t\tgrce  la technologie\n\t\t\tLED en Full HD 1080p !\n\t\t',
     *           u'id': u'screen001',
     *           u'name': u'Asus - VE228T - LED',
     *           u'price': u'118.30',
     *           u'qualityLevel': 4}}
     * </pre>     
     *                      
     * <pre>    
     * >>> pprint(s.dataserver.getItem("mouse001"))
     * {u'javaClass': u'java.util.HashMap',
     *  u'map': {u'description': u"Aujourd'hui, pour se satisfaire aux normes environnementales,\n\t\t\tnous nous devons de trouver de nouveaux matriaux pour la production.\n\t\t\tLe bambou est une de ces alternatives, avec des avantages cologiques\n\t\t\tprouvs tels que le recyclage facile et galement un tas de\n\t\t\tcaractristiques techniques intressantes.\n\t\t",
     *           u'id': u'mouse001',
     *           u'name': u'Souris en Bambou',
     *           u'price': u'25.00',
     *           u'qualityLevel': 4}}
     * </pre>
     *
     * @param unknown_type $aItemId
     */
    private function rpcGetItem($aItemId){

        /*
         * To access the client after loading the jsonrpc library, you can call
        * $this->jsonrpc->get_client(), which returns the client object.
        * You may want to pass this by reference (i.e. $my_client =& $this->jsonrpc->get_client()),
        * although unless you're requesting data from a large number of sources,
        * this shouldn't be a big issue.
        */
        $wJsonrpcClient =& $this->jsonrpc->get_client();

        /*
         * First, you need to set the server with $client->server().
        * The server function takes three arguments, only the first is required.
        * The first argument is the URI to request the data from,
        * the second argument is the method (either GET or POST, case-sensitive, defaults to POST),
        * and the third is the port number (defaults to 80).
        */
        $wJsonrpcClient->server('http://localhost/JSON-RPC','POST',$this->DataServerPort);

        /*
         * You can then specify a method with $client->method().
        * Method takes a single string representing the JSON-RPC method.
        * This may be empty if you are querying a JSON resource that doesn't adhere to the JSON-RPC spec.
        */
        $wJsonrpcClient->method('dataserver.getItem').

        /*
         * You can specify parameters with $client->request(), which takes an array representing
        * the request parameters.
        */
        $wParams = array();
        array_push($wParams,$aItemId);
        $wJsonrpcClient->request($wParams);

        $wJsonrpcClient->timeout(5);

        if(log_isOn('OFF')){
            log_message('INFO', "** Item_model.rpcGetItem() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );
        }

        $wJsonObject = $wJsonrpcClient->send_request();

        if ($wJsonObject != true){
            if(log_isOn('ERROR')){
                log_message('ERROR', "** Item_model.rpcGetItem() : bad response" );
                //log_message('ERROR', "** Item_model.rpcGetItem() : get_response_[". var_export($wJsonrpcClient->get_response(),true)."]" );
            }
            return null;
        }

        if(log_isOn('INFO')){
            log_message('INFO', "** Item_model.rpcGetItem() : get_response_object=[". var_export($wJsonrpcClient->get_response_object(),true)."]" );
        }

        $wData = $wJsonrpcClient->get_response_object();
        
        $wItem = array();
        
        if(isset($wData->result->map->error)){
        	
        	$wErrorBean = $wData->result->map->error;
        	$wItem['id'] = "00000";
        	$wItem['name'] = $wErrorBean->message;
        	$wItem['price'] = 0;
        	$wItem['qualityLevel'] = 0;
        	$wItem['description'] = $wErrorBean->javaClass;
        }else{

	        /*
	         * 
				>>> pprint(s.dataserver.getItem("mouse001"))
				{u'javaClass': u'java.util.HashMap',
				 u'map': {u'description': u"Aujourd'hui, pour se satisfaire ... essantes.\n\t\t",
				          u'id': u'mouse001',
				          u'name': u'Souris en Bambou',
				          u'price': u'25.00',
				          u'qualityLevel': 0}}
	         * 
	         * 
				stdClass::__set_state(array(
				   'id' => 'ID_988518742',
				   'result' => 
				  stdClass::__set_state(array(
				     'map' => 
				    stdClass::__set_state(array(
				       'id' => 'mouse012',
				       'price' => '15.50',
				       'qualityLevel' => 0,
				       'description' => 'Souris optique USB "For Business". Actus dicatur bonus qui est conformis legi et rationi ',
				       'name' => 'Microsoft Basic Optical Mouse',
				    )),
				     'javaClass' => 'java.util.HashMap',
				  )),
				))
	         * 
	         */
	        
	        $wItemBean = $wData->result->map;
	
	        $wItem['id'] = $wItemBean->id;
	        $wItem['name'] = $wItemBean->name;
	        $wItem['price'] = $wItemBean->price;
	        $wItem['qualityLevel'] = $wItemBean->qualityLevel;
	        $wItem['description'] = $wItemBean->description;
        }
        return $wItem;
    }

    /**
     * return an array of items asking the dataserver
     *
     * <pre>
			>>> pprint(s.dataserver.getItems("screens",2,False,""))
			{u'javaClass': u'java.util.LinkedList',
			 u'list': [{u'javaClass': u'java.util.HashMap',
			            u'map': {u'description': u'21.5" (54.6 cm) - Full HD (1920x1080) - 5 ms - 250 cd/m - DVI-D\n\t\t\t/ VGA / Audio\n\t\t\tMoniteur Asus VE228T. Profitez d\'images plus ralistes\n\t\t\tgrce  la technologie\n\t\t\tLED en Full HD 1080p !\n\t\t',
			                     u'id': u'screen001',
			                     u'name': u'Asus - VE228T - LED',
			                     u'price': u'118.30',
			                     u'qualityLevel': 0}},
			           {u'javaClass': u'java.util.HashMap',
			            u'map': {u'description': u'21.5" - 1920 x 1080 (16/9) - 5 ms - VGA / DVI-D (HDCP)\n\t\t\tEcran\n\t\t\tHANNS G utilisant la technologie LCD/LED de 21.5". Rfrence :\n\t\t\tHL225DBB. Propose une connectique VGA et DVI-D (HDCP).\n\t\t',
			                     u'id': u'screen002',
			                     u'name': u'PROMO - HANNS G - LED - HL225DBB',
			                     u'price': u'150.00',
			                     u'qualityLevel': 0}}]}
     * </pre>
     * @param String $aCategorie
     * @param Integer $aNbItems
     * @param boolean $aNb
     * @param String $aBaseId
     */
    private function rpcGetItems($aCategorie, $aNbItems, $aRandom,$aBaseId)
    {

        $wJsonrpcClient =& $this->jsonrpc->get_client();
        $wJsonrpcClient->server('http://localhost/JSON-RPC','POST',$this->DataServerPort);
        $wJsonrpcClient->method('dataserver.getItems').

        $wParams = array();
        array_push($wParams,$aCategorie);
        array_push($wParams,$aNbItems);
        array_push($wParams,$aRandom);
        array_push($wParams,$aBaseId);
        $wJsonrpcClient->request($wParams);

        $wJsonrpcClient->timeout(5);

        if(log_isOn('OFF')){
            log_message('INFO', "** Item_model.rpcGetItems() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );
        }

        $wJsonObject = $wJsonrpcClient->send_request();

        if ($wJsonObject != true){
            log_message('ERROR', "** Item_model.rpcGetItems() : bad response" );
            //log_message('ERROR', "** Item_model.rpcGetItems() : get_response_[". var_export($wJsonrpcClient->get_response(),true)."]" );
            return null;
        }

        if(log_isOn('INFO')){
            log_message('INFO', "** Item_model.rpcGetItems() : get_response_object=[". var_export($wJsonrpcClient->get_response_object(),true)."]" );
        }

        $wData = $wJsonrpcClient->get_response_object();
        
        $wItems = array();
        
        
        if(isset($wData->result->map->error)){
        	
        	/*
        	 * 
				stdClass::__set_state(array(
				   'id' => 'ID_759847750',
				   'result' => 
				  stdClass::__set_state(array(
				     'map' => 
				    stdClass::__set_state(array(
				       'error' => 
				      stdClass::__set_state(array(
				         'message' => 'Index: 0, Size: 0',
				         'localizedMessage' => 'Index: 0, Size: 0',
				         'cause' => NULL,
				         'javaClass' => 'java.lang.IndexOutOfBoundsException',
				         'stackTrace' => 
				        array (
				          0 => 
				          stdClass::__set_state(array(
				             'fileName' => 'LinkedList.java',
				             'javaClass' => 'java.lang.StackTraceElement',
				             'lineNumber' => 365,
				             'className' => 'java.util.LinkedList',
				             'methodName' => 'entry',
				             'nativeMethod' => false,
				          )),
				
				          32 => 
				          stdClass::__set_state(array(
				             'fileName' => 'QueuedThreadPool.java',
				             'javaClass' => 'java.lang.StackTraceElement',
				             'lineNumber' => 582,
				             'className' => 'org.mortbay.thread.QueuedThreadPool$PoolThread',
				             'methodName' => 'run',
				             'nativeMethod' => false,
				          )),
				        ),
				      )),
				    )),
				     'javaClass' => 'java.util.HashMap',
				  )),
				))          
        	 * 
        	 */
        	$wErrorBean = $wData->result->map->error;
        	$wItem = array();
        	$wItem['id'] = "00000";
        	$wItem['name'] = $wErrorBean->message;
        	$wItem['price'] = 0;
        	$wItem['qualityLevel'] = 0;
        	$wItem['description'] = $wErrorBean->javaClass;
        	array_push($wItems,$wItem);
        	
        } else{
	        /*
	         * 
				>>> pprint(s.dataserver.getItems("screens",2,False,""))
				{u'javaClass': u'java.util.LinkedList',
				 u'list': [{u'javaClass': u'java.util.HashMap',
				            u'map': {u'description': u'21.5" (54.6 cm) - Full HD (1920x1080) - 5 ms - 250 cd/m - DVI-D\n\t\t\t/ VGA / Audio\n\t\t\tMoniteur Asus VE228T. Profitez d\'images plus ralistes\n\t\t\tgrce  la technologie\n\t\t\tLED en Full HD 1080p !\n\t\t',
				                     u'id': u'screen001',
				                     u'name': u'Asus - VE228T - LED',
				                     u'price': u'118.30',
				                     u'qualityLevel': 0}},
				           {u'javaClass': u'java.util.HashMap',
				            u'map': {u'description': u'21.5" - 1920 x 1080 (16/9) - 5 ms - VGA / DVI-D (HDCP)\n\t\t\tEcran\n\t\t\tHANNS G utilisant la technologie LCD/LED de 21.5". Rfrence :\n\t\t\tHL225DBB. Propose une connectique VGA et DVI-D (HDCP).\n\t\t',
				                     u'id': u'screen002',
				                     u'name': u'PROMO - HANNS G - LED - HL225DBB',
				                     u'price': u'150.00',
				                     u'qualityLevel': 0}}]}
	         *
	         * 
				stdClass::__set_state(array(
				   'id' => 'ID_988518742',
				   'result' => 
				  stdClass::__set_state(array(
				     'javaClass' => 'java.util.LinkedList',
				     'list' => 
				    array (
				      0 => 
				      stdClass::__set_state(array(
				         'map' => 
				        stdClass::__set_state(array(
				           'id' => 'screen001',
				           'price' => '118.30',
				           'qualityLevel' => 0,
				           'description' => '21.5" (54.6 cm) - Full HD (1920x1080) - 5 ms - 250 cd/m - DVI-D....',
				           'name' => 'Asus - VE228T - LED',
				        )),
				         'javaClass' => 'java.util.HashMap',
				      )),
				...
				      5 => 
				      stdClass::__set_state(array(
				         'map' => 
				        stdClass::__set_state(array(
				           'id' => 'screen006',
				           'price' => '165.50',
				           'qualityLevel' => 0,
				           'description' => '21.5" Full HD (1920x1080) - 2ms - D-Sub / HDMI...',
				           'name' => 'Asus - ML228H - LED',
				        )),
				         'javaClass' => 'java.util.HashMap',
				      )),
				    ),
				  )),
				))
	         * 
	         */
	        $wItemBeans = $wData->result->list;
	
	        foreach ($wItemBeans as $wIdB=>$wItemBean) {
	            $wItemBeanContent = $wItemBean->map;
	            $wItem = array();
	            $wItem['id'] = $wItemBeanContent->id;
	            $wItem['name'] = $wItemBeanContent->name;
	            $wItem['price'] = $wItemBeanContent->price;
	            $wItem['qualityLevel'] = $wItemBeanContent->qualityLevel;
	            $wItem['description'] = $wItemBeanContent->description;
	            array_push($wItems,$wItem);
	        }
		}
        return $wItems;
    }

    /**
     *
     * return a array of item beans containing a stock quantity asking the data server
     *
     *
     *
     * <pre>
     *  >>> pprint(s.dataserver.getItemsStock(["screen001","mouse001"]))
     * 	{	u'javaClass': u'java.util.LinkedList',
     * 		u'list': [	{	u'javaClass': u'java.util.HashMap',
     * 	 					u'map': {	u'id': u'screen001', 
     * 	 								u'qualityLevel': 0, 
     * 	 								u'stock': 149}},
     * 					{	u'javaClass': u'java.util.HashMap',
     *  					u'map': {	u'id': u'mouse001', 
     *  								u'qualityLevel': 0, 
     * 									u'stock': 195}}]}
     * 
     * </pre>
     *
     *
     * @param Array $aItemIds
     */
    private function rpcGetItemsStock($aItemIds){

        $wJsonrpcClient =& $this->jsonrpc->get_client();
        $wJsonrpcClient->server('http://localhost/JSON-RPC','POST',$this->DataServerPort);
        $wJsonrpcClient->method('dataserver.getItemsStock').

        $wParams = array();
        array_push($wParams,$aItemIds);
        $wJsonrpcClient->request($wParams);

        $wJsonrpcClient->timeout(5);

        if(log_isOn('OFF')){
            log_message('INFO', "** Item_model.rpcGetItemsStock() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );
        }

        $wJsonObject = $wJsonrpcClient->send_request();


        if ($wJsonObject != true){
            log_message('ERROR', "** Item_model.rpcGetItemsStock() : bad response" );
            //log_message('ERROR', "** Item_model.rpcGetItemsStock() : get_response_[". var_export($wJsonrpcClient->get_response(),true)."]" );
            return null;
        }

        if(log_isOn('INFO')){
            log_message('INFO', "** Item_model.rpcGetItemsStock() : get_response_object=[". var_export($wJsonrpcClient->get_response_object(),true)."]" );
        }

        $wData = $wJsonrpcClient->get_response_object();
        
        $wItems = array();
        
        if(isset($wData->result->map->error)){
        	
        	$wErrorBean = $wData->result->map->error;
        	 
        	$wItem = array();
        	$wItem['id'] =  $wErrorBean->message;
        	$wItem['stock'] = -1;
        	$wItem['qualityLevel'] = -1;
        	array_push($wItems,$wItem);
        }else{
	        /*
	         * 
				>>> pprint(s.dataserver.getItemsStock(["screen001","mouse001"]))
				{u'javaClass': u'java.util.LinkedList',
				 u'list': [{u'javaClass': u'java.util.HashMap',
				            u'map': {u'id': u'screen001', u'qualityLevel': 0, u'stock': 149}},
				           {u'javaClass': u'java.util.HashMap',
				            u'map': {u'id': u'mouse001', u'qualityLevel': 0, u'stock': 195}}]}
	         * 
	         * 
				stdClass::__set_state(array(
				   'id' => 'ID_1002671583',
				   'result' => 
				  stdClass::__set_state(array(
				     'javaClass' => 'java.util.LinkedList',
				     'list' => 
				    array (
				      0 => 
				      stdClass::__set_state(array(
				         'map' => 
				        stdClass::__set_state(array(
				           'id' => 'screen001',
				           'stock' => 149,
				           'qualityLevel' => 0,
				        )),
				         'javaClass' => 'java.util.HashMap',
				      )),
				...
				      5 => 
				      stdClass::__set_state(array(
				         'map' => 
				        stdClass::__set_state(array(
				           'id' => 'screen006',
				           'stock' => 49,
				           'qualityLevel' => 0,
				        )),
				         'javaClass' => 'java.util.HashMap',
				      )),
				    ),
				  )),
				))
	         * 
	         * 
	         */
	        $wItemBeans = $wData->result->list;
	
	        foreach ($wItemBeans as $wIdB=>$wItemBean) {
	
	            $wItemBeanContent = $wItemBean->map;
	
	            $wItem = array();
	            $wItem['id'] = $wItemBeanContent->id;
	            $wItem['stock'] = $wItemBeanContent->stock;
	            $wItem['qualityLevel'] = $wItemBeanContent->qualityLevel;
	            array_push($wItems,$wItem);
	        }
        }
        return $wItems;
    }

    //************************************************************************************************
    // LOCAL DATA
    //************************************************************************************************

    /**
     * retrieve an Item reading the local data ( items.xml)
     *
     * @param unknown_type $aItemId
     */
    private function localGetItem($aItemId){
        log_message('debug', "** CItem.localGetItem() : [".$aItemId."]");

        $wItems = $this->localBuildItems();

        if ($aItemId == '?'){
            $wRandomIdx = rand(1, 21);
            $wRandomIdx = str_pad($wRandomIdx, 3, "0", STR_PAD_LEFT);
            $wRandomTyp = rand(0, 1);
            $aItemId = (($wRandomTyp==0)?'screen':'mouse').$wRandomIdx;
        }

        foreach ($wItems as $wId=>$wItem) {
            if ($wItem['id']==$aItemId){
                return $wItem;
            }
        }
        return null;
    }

    /**
     * return an array of items reading the local data ( items.xml)
     *
     * @param String $aCategorie
     * @param Integer $aNbItems
     * @param boolean $aNb
     * @param String $aBaseId
     */
    private function localGetItems($aCategorie, $aNbItems, $aRandom,$aBaseId){

        log_message('debug', "** CItem.localGetItems() : [".$aCategorie."][".$aNbItems."][".$aRandom."][".$aBaseId."]");

        $wItems = $this->localBuildItems();

        if ($aCategorie != ''){
            $wItems = $this->localSubsetCategorie($wItems,$aCategorie);
        }

        if ( $aNbItems<99 ){
            $wItems = $this->localSubsetPage($wItems,$aNbItems, $aRandom,$aBaseId);
        }


        return $wItems;
    }

    /**
     *
     * return a array of item beans containing a stock quantity reading the local data ( items.xml)
     *
     * @param Array $aItemIds
     */
    private function localGetItemsStock($aItemIds){

        $wItems = array();
        foreach ($aItemIds as $wId=>$wItemId) {

            $wItem = array();

            $wItem['id'] = $wItemId;
            $wQuantity = rand(0,100);
            $wItem['stock'] = $wQuantity;
            /*
             * 0 : SYNC Top qualité (retour direct de l’ERP)
            * 1 : FRESH Cache niveau 1 (moins de 1 minute (incluses) depuis la mise en cache)
            * 2 : ACCEPTABLE Cache niveau 2 (moins de 5 minutes (incluses) depuis la mise en cache)
            * 3 : WARNING Cache niveau 3 (moins de 15 minutes (incluses) depuis la mise en cache)
            * 4 : CRITICAL Qualité critique (plus de 15 minutes depuis la mise en cache)
            */
            //$wItem['stockquality'] = ($wStock<10)?}:($wStock<30)?3: rand(0,2);

            if ($wQuantity<10){
                $wItem['qualityLevel'] = 4;
            }
            else if ($wQuantity<30){
                $wItem['qualityLevel'] = 3;
            } else if ($wQuantity<50){
                $wItem['qualityLevel'] = 2;
            }else if ($wQuantity<75){
                $wItem['qualityLevel'] = 1;
            }else {
                $wItem['qualityLevel'] = 0;
            }

            array_push($wItems,$wItem);

        }
        return $wItems;
    }


    /**
     * Return a subset of the items according to the passed category
     *
     * @param Array $aItems
     * @param String $aCategorie
     * @return Array
     */
    private function localSubsetCategorie($aItems,$aCategorie){

        // remove the last letter
        $wPrefix = substr($aCategorie, 0, -1);

        log_message('debug', "** CItem.subsetCategorie() : [".$aCategorie."][".$wPrefix."]");

        $wItems = array();
        foreach ($aItems as $wId=>$wItem) {

            // if the prefix is found un the item id
            if (strpos($wItem['id'], $wPrefix) !== false){
                array_push($wItems,$wItem);
            }
        }
        return $wItems;
    }

    /**
     * Return a subset of n items according to the start index or the randomization
     *
    * @param Integer $aNbItems
    * @param boolean $aNb
    * @param String $aBaseId
     * @return Array
     */
    private function localSubsetPage($aItems, $aNbItems, $aRandom,$aBaseId){

        $wItems = array();

        $wStartIdx  =  ($aRandom)?0 : $this->findStartIdx($aItems,$aBaseId);

        $wMax = count($aItems);
        if ($aNbItems +$wStartIdx>$wMax){
            $aNbItems=$wMax-$wStartIdx;
        }
        for ($wI = $wStartIdx; $wI < $wStartIdx+$aNbItems; $wI++) {
            $wIdx = ($aRandom)? rand(0, $wMax-1): $wI;
            array_push($wItems,$aItems[$wIdx]);
        }

        return $wItems;
    }

    /**
     * @return the start index according the passed base id.
     */
    private function findStartIdx($aItems,$aBaseId){
        $wI = 0;
        foreach ($aItems as $wId=>$wItem) {
            // if the id is greater than $aBaseId
            if ($wItem['id'] > $aBaseId){
                return $wI;
            }
            $wI++;
        }
        return 0;
    }


    /**
     *
     * @return Array An array of items. Each item is an array
     */
    private function localBuildItems(){
        $wXml = new CXml();

        if(  $wXml->load("./app_resources/data/items.xml") != true){
            return "loading error";
        }
        $wItems = $wXml->parse();
        return $this->localSimplfyItemsArray($wItems);

    }

    /**
     * Reformat the array of items produced by the CXml tool
     *
     * @param Array $aItems
     * @return Array An array of items. Each item is an array
     */
    private function localSimplfyItemsArray($aItems){

        $wItems = array();

        // items
        foreach($aItems as $id1=>$sub1) {

            //0
            foreach($sub1 as $id2=>$sub2) {

                //item
                foreach($sub2 as $id3=>$sub3) {

                    // 0 ==> 28
                    foreach($sub3 as $id4=>$sub4) {

                        $wItem = array();
                        foreach($sub4 as $id5=>$sub5) {

                            foreach($sub5 as $id6=>$sub6) {
                                $wItem[$id5]=$sub6;
                            }

                        }
                        array_push($wItems,$wItem);
                    }
                }
            }
        }
        return $wItems;
    }

}
?>
