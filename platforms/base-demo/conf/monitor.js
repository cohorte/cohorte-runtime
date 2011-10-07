{
    "id":"org.psem2m.internals.isolates.monitor-1",
    "kind":"felix",
    "httpPort":9100,
    "vmArgs":[
              ],
    "bundles":[
        {
            "symbolicName":"org.psem2m.isolates.demo.services.ui.viewer",
            "optional":true,
            "properties":{
            	"psem2m.demo.ui.viewer.top":"0.66scr",
            	"psem2m.demo.ui.viewer.left":"0scr",
            	"psem2m.demo.ui.viewer.width":"0.166scr",
            	"psem2m.demo.ui.viewer.height":"0.33scr",
            	"psem2m.demo.ui.viewer.color":"SkyBlue"
            }
        },
        {
            "symbolicName":"org.apache.felix.shell"
        },
        {
            "symbolicName":"org.apache.felix.shell.remote",
            "properties":{
            	"osgi.shell.telnet.port":"6000"
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
            "from":"remote-services.js",
            "overide":[{
            	"symbolicName":"org.psem2m.isolates.remote.importer",
            	"properties":{
                	"org.psem2m.isolates.remote.importer.excludes":"org.psem2m.demo.*"
                }
            }]
        },
        {
            "symbolicName":"org.psem2m.isolates.master.manager"
        },
        {
            "symbolicName":"org.psem2m.isolates.monitor"
        }
    ]
}