<?php

class Cart_model extends CI_Model {

	private $DataServerPort = 9210;
	
	/**
	 *
	 * PHP5 constructor
	 */
	function __construct(){
		parent::__construct();
		log_message('debug', "** Cart_model.[init] [".$this->DataServerPort."]");

		$this->load->library('jsonrpc');
	}
	
	/**
	 * Submits an array of cart lines to the ERP
	 * 
	 * 
	 * public String getItemId()
	 * public String getLineId() 
	 * public double getQuantity()
	 * 
	 * @param Array $aCartLines an array of cart lines
	 */
	public function applyCart($aCartLines){
		
		$wResponse =  $this->rpcApplyCart($aCartLines);
		if (!is_null($wResponse)){
				return $wResponse;
		}
		return  $this->localApplyCart($aCartLines);
	}
	
	/**
	 * 
	 * @param Array $aCart
	 * @return NULL
	 */
	private function rpcApplyCart($aCart){
	
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
		$wJsonrpcClient->method('dataserver.applyCart').
		
		/*
		 * You can specify parameters with $client->request(), which takes an array representing
		* the request parameters.
		*/
		$wParams = array();
		array_push($wParams,$aCart);
		$wJsonrpcClient->request($wParams);
		
		$wJsonrpcClient->timeout(5);
		
		if(log_isOn('INFO')){
			log_message('INFO', "** Cart_model.rpcApplyCart() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );
		}
		
		$wJsonObject = $wJsonrpcClient->send_request();
		
		if ($wJsonObject != true){
			log_message('ERROR', "** Cart_model.rpcApplyCart() : get_response_[". var_export($wJsonrpcClient->get_response(),true)."]" );
			return null;
		}
		
		if(log_isOn('INFO')){
			log_message('INFO', "** Cart_model.rpcApplyCart() : get_response_object=[". var_export($wJsonrpcClient->get_response_object(),true)."]" );
		}

		$wData = $wJsonrpcClient->get_response_object();
		
		
		/*
		 * 
			gstdClass::__set_state(array(
			   'id' => 'ID_1813285042',
			   'error' => 
			  stdClass::__set_state(array(
			     'code' => 592,
			     'msg' => 'arg 1 could not unmarshall',
			  )),
			))
		 * 
		 * 
			stdClass::__set_state(array(
			   'id' => 'ID_1010158050',
			   'result' => 
			  stdClass::__set_state(array(
			     'map' => 
			    stdClass::__set_state(array(
			       'result' => 
			      array (
			      ),
			       'error' => 
			      array (
			        0 => 'DataServerApplication.CartsApplier2.safeErpCaller : Exception caught
			
			class=[org.jabsorb.client.ErrorResponse]
			mess(1)=[JSONRPC error code -32603:  | ErrorResponse | org.jabsorb.client.Client(processException:227)]
			stack=[org.jabsorb.client.Client(processException:227)
			org.jabsorb.client.Client(invoke:174)
			org.jabsorb.client.Client(invoke:135)
			$Proxy1(applyCart:-1)
			org.psem2m.composer.demo.erpproxy.ErpProxy(__M_applyCart:92)
			org.psem2m.composer.demo.erpproxy.ErpProxy(applyCart:-1)
			org.psem2m.composer.demo.erpproxy.ErpProxy(__M_computeResult:119)
			org.psem2m.composer.demo.erpproxy.ErpProxy(computeResult:-1)
			org.psem2m.composer.test.api.IComponent$$Proxy(computeResult:-1)
			org.psem2m.composer.demo.impl.ErpCaller(__M_computeResult:59)
			org.psem2m.composer.demo.impl.ErpCaller(computeResult:-1)
			org.psem2m.composer.test.api.IComponent$$Proxy(computeResult:-1)
			org.psem2m.composer.core.test.chain.ExceptionCatcher(__M_computeResult:63)
			org.psem2m.composer.core.test.chain.ExceptionCatcher(computeResult:-1)
			',
			      ),
			    )),
			     'javaClass' => 'java.util.HashMap',
			  )),
			))		 
		 * 
		 * 
		 */
		
		if (isset($wData->map->error)){
			$wErpActionError = $wData->map->error;
			return  array('status'=>$wErpActionError->code,'message'=>$wErpActionError->msg,'reasonInfos'=>'');
		}
		
		/*
		 * 
			stdClass::__set_state(array(
			   'id' => 'ID_979626130',
			   'result' => 
			  stdClass::__set_state(array(
			     'map' => 
			    stdClass::__set_state(array(
			       'message' => 'Cart applied',
			       'reason' => '',
			       'code' => 200,
			    )),
			     'javaClass' => 'java.util.HashMap',
			  )),
			))
		*
		*
			stdClass::__set_state(array(
			   'id' => 'ID_1853053347',
			   'result' => 
			  stdClass::__set_state(array(
			     'map' => 
			    stdClass::__set_state(array(
			       'message' => 'Insufficient stock',
			       'reason' => 'Can\'t get 100mouse001',
			       'code' => 500,
			    )),
			     'javaClass' => 'java.util.HashMap',
			  )),
			))		 
		*/
		$wErpActionReport = $wData->result->map;
		
		return  array('status'=>$wErpActionReport->code,'message'=>$wErpActionReport->message,'reasonInfos'=>$wErpActionReport->reason);
	}
	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aCartLines
	 */
	private function localApplyCart($aCartLines){
	
	
		return  array('status'=>'501','message'=>'not implemented','reasonInfos'=>'localApplyCart() is not implemented in theWebStore: must call the DataServer.');
	}
	
}

?>