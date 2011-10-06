<?php

class Cart_model extends CI_Model {

	/**
	 *
	 * PHP5 constructor
	 */
	function __construct(){
		parent::__construct();
		log_message('debug', "** Cart_model.[init]");

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
		if ($wResponse!=null){
			return $wResponse;
		}
		return  $this->localApplyCart($aCartLines);
	}
	
	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aCartLines
	 * @return NULL
	 */
	private function rpcApplyCart($aCartLines){
	
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
		$wJsonrpcClient->server('http://localhost/JSON-RPC','POST',9210);
		
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
		array_push($wParams,$aCartLines);
		$wJsonrpcClient->request($wParams);
		
		$wJsonrpcClient->timeout(5);
		
		//log_message('INFO', "** Item_model.rpcGetItem() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );
		
		$wJsonObject = $wJsonrpcClient->send_request();
		
		if ($wJsonObject != true){
			log_message('ERROR', "** Item_model.rpcGetItem() : get_response_[". var_export($wJsonrpcClient->get_response(),true)."]" );
			return null;
		}
		
		log_message('INFO', "** Item_model.rpcGetItem() : get_response_object=[". var_export($wJsonrpcClient->get_response_object(),true)."]" );

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
		 */
		
		if (isset($wData->error)){
			$wErpActionError = $wData->error;
			return  array('status'=>$wErpActionError->code,'message'=>$wErpActionError->msg,'reasonInfos'=>'');
		}
		
		
		/*
		*	stdClass::__set_state(array(
				'id' => 'ID_834117402',
				'result' =>
						stdClass::__set_state(array(
							'status' => 200,
							'message' => 'xxxx',
							'reasonInfos' => '',
							'qualityLevel' => 0,
							'javaClass' => 'org.psem2m.demo.erp.api.beans.CErpActionReport',
						))
				))
		*/
		$wErpActionReport = $wData->result;
		
		return  array('status'=>$wErpActionReport->status,'message'=>$wErpActionReport->message,'reasonInfos'=>$wErpActionReport->reasonInfos);
	}
	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aCartLines
	 */
	private function localApplyCart($aCartLines){
	
	
		return  array('status'=>'501','message'=>'not implemented','reasonInfos'=>'localApplyCart() must be implemented');
	}
	
}

?>