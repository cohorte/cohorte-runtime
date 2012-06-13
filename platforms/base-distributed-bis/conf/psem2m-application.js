{
    "appId":"distributed-app",
    "multicast":"ff15::42",
    "isolates":[
        {
            "from":"monitor.js"
        },
        {
            "from":"forker.js"
        },
        {
            "id":"demo.central",
            "kind":"pelix",
            "node":"central",
            "httpPort":9100,
            "bundles":[
                {
                    "symbolicName":"base.httpsvc"
                },
                {
                    "symbolicName":"base.signals.directory"
                },
                {
                    "symbolicName":"base.signals.http"
                },
                {
                    "symbolicName":"base.multicast_agent"
                },
                {
                    "symbolicName":"base.remoteservices"
                },
                {
                    "symbolicName":"base.composer"
                },
                {
                    "symbolicName":"provider"
                }
            ],
            "environment":{
                "PYTHONPATH":"/home/tcalmant/programmation/workspaces/psem2m/platforms/base-distributed-bis/bin"
            }
        },
        {
            "id":"demo.snowball",
            "kind":"pelix",
            "node":"snowball",
            "httpPort":9100,
            "bundles":[
                {
                    "symbolicName":"base.httpsvc"
                },
                {
                    "symbolicName":"base.signals.directory"
                },
                {
                    "symbolicName":"base.signals.http"
                },
                {
                    "symbolicName":"base.remoteservices"
                },
                {
                    "symbolicName":"base.composer"
                },
                {
                    "symbolicName":"consumer"
                }
            ]
        }
    ]
}