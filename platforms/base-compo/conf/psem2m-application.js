{
    "appId":"development-app",
    "isolates":[
        {
            "from":"monitor.js"
        },
        {
            "from":"forker.js"
        },
        {
            "id":"isolate-dataserver",
            "kind":"felix",
            "httpPort":9210,
            "vmArgs":[
            ],
            "bundles":[
				{
				    "symbolicName":"org.psem2m.isolates.ui.admin",
				    "optional":true,
				    "properties":{
				        "psem2m.demo.ui.viewer.top":"0scr",
				        "psem2m.demo.ui.viewer.left":"0.25scr",
				        "psem2m.demo.ui.viewer.width":"0.25scr",
				        "psem2m.demo.ui.viewer.height":"0.66scr",
				        "psem2m.demo.ui.viewer.color":"YellowGreen"
				    }
				},
				{
				    "symbolicName":"org.apache.felix.shell"
				},
				{
				    "symbolicName":"org.apache.felix.shell.remote",
				    "properties":{
				        "osgi.shell.telnet.port":"6002"
				    }
				},
				{
				    "from":"signals-http.js"
				},
				{
				    "from":"rose-core.js"
				},
				{
				    "from":"rose-client.js"
				},
				{
				    "from":"rose-server.js"
				},
				{
				    "from":"remote-services.js"
				},
				{
				    "symbolicName":"org.psem2m.composer.api"
				},
				{
				    "symbolicName":"org.psem2m.composer.agent"
				},
				{
				    "symbolicName":"org.psem2m.composer.demo.api"
				},
				{
				    "symbolicName":"org.psem2m.composer.demo.dataserver"
				}
            ]
        },
        {
            "id":"isolate-cache",
            "kind":"felix",
            "httpPort":9211,
            "vmArgs":[
                
            ],
            "bundles":[
                {
                    "symbolicName":"org.psem2m.isolates.ui.admin",
                    "optional":true,
                    "properties":{
                        "psem2m.demo.ui.viewer.top":"0scr",
                        "psem2m.demo.ui.viewer.left":"0.50scr",
                        "psem2m.demo.ui.viewer.width":"0.25scr",
                        "psem2m.demo.ui.viewer.height":"0.66scr",
                        "psem2m.demo.ui.viewer.color":"YellowGreen"
                    }
                },
                {
                    "symbolicName":"org.apache.felix.shell"
                },
                {
                    "symbolicName":"org.apache.felix.shell.remote",
                    "properties":{
                        "osgi.shell.telnet.port":"6003"
                    }
                },
                {
                    "from":"signals-http.js"
                },
                {
                    "from":"rose-core.js"
                },
                {
                    "from":"rose-client.js"
                },
                {
                    "from":"rose-server.js"
                },
                {
                    "from":"remote-services.js"
                },
                {
                    "symbolicName":"org.psem2m.composer.api"
                },
                {
                    "symbolicName":"org.psem2m.composer.agent"
                },
                {
                    "symbolicName":"org.psem2m.demo.data.cache.api"
                },
                {
                    "symbolicName":"org.psem2m.demo.data.cache"
                },
                {
                    "symbolicName":"org.psem2m.composer.demo.api"
                },
                {
                    "symbolicName":"org.psem2m.composer.demo"
                },
                {
                    "symbolicName":"org.psem2m.composer.demo.cache"
                }
            ]
        },
        {
            "id":"isolate-erpproxy",
            "kind":"felix",
            "httpPort":9212,
            "vmArgs":[
            ],
            "bundles":[
                {
                    "symbolicName":"org.psem2m.isolates.ui.admin",
                    "optional":true,
                    "properties":{
                        "psem2m.demo.ui.viewer.top":"0scr",
                        "psem2m.demo.ui.viewer.left":"0.75scr",
                        "psem2m.demo.ui.viewer.width":"0.25scr",
                        "psem2m.demo.ui.viewer.height":"0.66scr",
                        "psem2m.demo.ui.viewer.color":"YellowGreen"
                    }
                },
                {
                    "symbolicName":"org.apache.felix.shell"
                },
                {
                    "symbolicName":"org.apache.felix.shell.remote",
                    "properties":{
                        "osgi.shell.telnet.port":"6004"
                    }
                },
                {
                    "from":"signals-http.js"
                },
                {
                    "from":"rose-core.js"
                },
                {
                    "from":"rose-client.js"
                },
                {
                    "from":"rose-server.js"
                },
                {
                    "from":"remote-services.js"
                },
                {
                    "symbolicName":"org.psem2m.composer.api"
                },
                {
                    "symbolicName":"org.psem2m.composer.agent"
                },
                {
                    "symbolicName":"org.psem2m.composer.demo.api"
                },
                {
                    "symbolicName":"org.psem2m.composer.demo.erpproxy"
                }
            ]
        }
    ]
}