=============================================================================

                     JSON-RPC for CodeIgniter 1.7

             Written by Nicholas Husher (nhusher@bear-code.com)

=============================================================================

Approximately conforms to the JSON-RPC 1.1 working draft. Because it's a working draft, there are certain aspects to it that are unfinished but, as a whole it is a significant improvement in quality over JSON-RPC 1.0. The server *should* be backwards-compatible with JSON-RPC 1.0, although this is untested.

The Jsonrpc server comes in two parts, a client and a server. The client is used to request JSON information from remote sources, the serve is used to serve (mostly-)valid JSON-RPC content to requesting resources. The client supports requesting data via both GET and POST, while the server only responds to POST requests for the time being.

Requirements:
* PHP5 or other PHP with a defined json_encode function.
* CodeIgniter (tested on v1.7)

Installation:
1. Drop the file 'Jsonrpc.php' into your libraries folder
2. That's it!

Using the JSON-RPC library:
To use the library, load it in CodeIgniter. This can be done with $this->load->library('jsonrpc'). From there, you can access the client or server functionality with $this->jsonrpc->get_client() and $this->jsonrpc->get_server(). Both the client and the server were modeled of of CodeIgniter's included XML-RPC libraries, although there are certain differences.

Using the Client
To access the client after loading the jsonrpc library, you can call $this->jsonrpc->get_client(), which returns the client object. You may want to pass this by reference (i.e. $my_client =& $this->jsonrpc->get_client()), although unless you're requesting data from a large number of sources, this shouldn't be a big issue.

After you have your client object, it behaves similarly to the CI XML-RPC library. First, you need to set the server with $client->server(). The server function takes three arguments, only the first is required. The first argument is the URI to request the data from, the second argument is the method (either GET or POST, case-sensitive, defaults to POST), and the third is the port number (defaults to 80).

You can then specify a method with $client->method(). Method takes a single string representing the JSON-RPC method. This may be empty if you are querying a JSON resource that doesn't adhere to the JSON-RPC spec.

You can specify parameters with $client->request(), which takes an array representing the request parameters.

Using the Server:
To access the server after loading the jsonrpc library, you can call $this->jsonrpc->get_server(). As above, it may be a good idea to pass this by reference rather than by value.

After you have your server object, you can specify the functions you would like to serve. This is similar to defining functions with the XML-RPC library, but with a few important differences.

First, you need to define a list of functions. This can be done with the $server->define_methods() function, which takes an array of a particular format as its only argument. This array defines all the functions the server knows how to serve.

Second, you need to set the object that contains the JSON-RPC functions. This is usually $this, but may be something different if you choose. You can do this with the $server->set_object() function. This is equivalent to the 'object' property of the CI XML-RPC configuration object.

Last, to serve the content simply call the $server->serve() function.