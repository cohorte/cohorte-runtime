<?php

class Item_model extends CI_Model {

	/**
	 *
	 * PHP5 constructor
	 */
	function __construct(){
		parent::__construct();
		log_message('debug', "** CItem.[init]");

		$this->load->library('jsonrpc');
	}

	/**
	 * return the found Item bean (array)
	 *
	 * The returned bean (array) contains four values :
	 * - String  'id'
	 * - String  'lib'
	 * - String  'text'
	 * - Integer 'price'
	 *
	 * @param String $aItemId
	 * @return Array|NULL  an array of items
	 */
	public function getItem($aItemId){

		//$this->rpcGetItem($aItemId);

		return  $this->localGetItem($aItemId);
	}


	/**
	 * return an array of item beans (array)
	 *
	 * Each returned bean (array) contains four values :
	 * - String  'id'
	 * - String  'lib'
	 * - String  'text'
	 * - Integer 'price'
	 *
	 * @param String $aCategorie
	 * @param Integer $aSimpleList
	 * @param Integer $aNb
	 * @param Boolean $aRandom
	 */
	public function getItems($aCategorie='',$aStartIdx=0, $aNb=99, $aRandom=false)
	{
		return $this->rpcGetItems($aCategorie,$aStartIdx,$aNb,$aRandom);

		//return $this->localGetItems($aCategorie,$aStartIdx, $aNb, $aRandom);
	}

	/**
	 *
	 * return a array of item beans containing a stock quantity according the passed list of item ids.
	 *
	 * Each returned bean (array) contains three values :
	 * - String  'id'
	 * - Integer 'stock"
	 * - Integer 'stockquality"
	 *
	 * 0 : SYNC Top qualité (retour direct de l’ERP)
	 * 1 : FRESH Cache niveau 1 (moins de 1 minute (incluses) depuis la mise en cache)
	 * 2 : ACCEPTABLE Cache niveau 2 (moins de 5 minutes (incluses) depuis la mise en cache)
	 * 3 : WARNING Cache niveau 3 (moins de 15 minutes (incluses) depuis la mise en cache)
	 * 4 : CRITICAL Qualité critique (plus de 15 minutes depuis la mise en cache)
	 *
	 * @param Array $aItemIds
	 */
	public function getItemsStock($aItemIds){
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
		$wJsonrpcClient->server('http://localhost/JSON-RPC/dataserver','POST',9010);

		/*
		 * You can then specify a method with $client->method().
		* Method takes a single string representing the JSON-RPC method.
		* This may be empty if you are querying a JSON resource that doesn't adhere to the JSON-RPC spec.
		*/
		$wJsonrpcClient->method('getItem').

		/*
		 * You can specify parameters with $client->request(), which takes an array representing
		* the request parameters.
		*/
		$wParam = array();
		array_push($wParam,$aItemId);
		$wJsonrpcClient->request($wParam);

		/*
		 *
		*/
		$wJsonrpcClient->timeout(5);

		log_message('debug', "** Item_model.rpcGetItem() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );

		$wJsonrpcClient->send_request();


		return null;
	}

	/**
	 * return an array of items asking the dataserver
	 *
	 * @param unknown_type $aCategorie
	 * @param unknown_type $aStartIdx
	 * @param unknown_type $aNb
	 * @param unknown_type $aRandom
	 */
	private function rpcGetItems($aCategorie,$aStartIdx, $aNb, $aRandom)
	{

		$wJsonrpcClient =& $this->jsonrpc->get_client();
		$wJsonrpcClient->server('http://localhost/JSON-RPC','POST',9010);
		$wJsonrpcClient->method('dataserver.getItems').

		$wParam = array();
		array_push($wParam,$aCategorie);
		$wJsonrpcClient->request($wParam);

		$wJsonrpcClient->timeout(5);

		log_message('debug', "** Item_model.rpcGetItems() : wJsonrpcClient=[". var_export($wJsonrpcClient,true)."]" );

		$wJsonObject = $wJsonrpcClient->send_request();

		if ($wJsonObject == true){
			log_message('debug', "** Item_model.rpcGetItems() : get_response_object=[". var_export($wJsonrpcClient->get_response_object(),true)."]" );
		}else{
			log_message('debug', "** Item_model.rpcGetItems() : get_response_[". var_export($wJsonrpcClient->get_response(),true)."]" );
		}

		$wData = $wJsonrpcClient->get_response_object();
		/*
		 *	stdClass::__set_state(array(
			'id' => 'ID_834117402',
			'result' =>
				array (
					0 =>
						stdClass::__set_state(array(
							'id' => 'mouse001',
							'price' => '25.00 EUR',
							'qualityLevel' => 0,
							'description' => 'Aujourd\'hui ',
							'name' => 'Souris en Bambou',
							'javaClass' => 'org.psem2m.demo.erp.api.beans.CachedItemBean',
						)),
					1 => 
					    stdClass::__set_state(array(
					       'id' => 'mouse002',
					       'price' => '35.33 EUR',
					       'qualityLevel' => 0,
					       'description' => 'Souris sans fil',
					       'name' => 'Advance Arty POP Flower Mouse',
					       'javaClass' => 'org.psem2m.demo.erp.api.beans.CachedItemBean',
					    )),
				 ...
			))
		*/
		$wItemBeans = $wData->result;
		
		
		$wItems = array();
		
		foreach ($wItemBeans as $wIdB=>$wItemBean) {
			$wItem = array();
			$wItem['id'] = $wItemBean->id;
			$wItem['lib'] = $wItemBean->name;
			$wItem['price'] = $wItemBean->price;
			$wItem['qualityLevel'] = $wItemBean->qualityLevel;
			$wItem['text'] = $wItemBean->description;
			array_push($wItems,$wItem);
		}

		return $wItems;
	}

	/**
	 *
	 * return a array of item beans containing a stock quantity asking the data server
	 *
	 * @param Array $aItemIds
	 */
	private function rpcGetItemsStock($aItemIds){
		return  null;
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

		$wItems = $this->localBuildItems();

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
	 * @param unknown_type $aCategorie
	 * @param unknown_type $aStartIdx
	 * @param unknown_type $aNb
	 * @param unknown_type $aRandom
	 */
	private function localGetItems($aCategorie,$aStartIdx, $aNb, $aRandom)
	{
		log_message('debug', "** CItem.getItems() : [".$aCategorie."][".$aStartIdx."][".$aNb."][".$aRandom."]");

		$wItems = $this->localBuildItems();

		if ($aCategorie != ''){
			$wItems = $this->localSubsetCategorie($wItems,$aCategorie);
		}

		if ($aStartIdx>0 || $aNb<99){
			$wItems = $this->localSubsetPage($wItems,$aStartIdx,$aNb,$aRandom);
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
			$wStock = rand(0,100);
			$wItem['stock'] = $wStock;
			/*
			 * 0 : SYNC Top qualité (retour direct de l’ERP)
			* 1 : FRESH Cache niveau 1 (moins de 1 minute (incluses) depuis la mise en cache)
			* 2 : ACCEPTABLE Cache niveau 2 (moins de 5 minutes (incluses) depuis la mise en cache)
			* 3 : WARNING Cache niveau 3 (moins de 15 minutes (incluses) depuis la mise en cache)
			* 4 : CRITICAL Qualité critique (plus de 15 minutes depuis la mise en cache)
			*/
			//$wItem['stockquality'] = ($wStock<10)?}:($wStock<30)?3: rand(0,2);
				
			if ($wStock<10){
				$wItem['stockquality'] = 4;
			}
			else if ($wStock<30){
				$wItem['stockquality'] = 3;
			} else if ($wStock<50){
				$wItem['stockquality'] = 2;
			}else if ($wStock<75){
				$wItem['stockquality'] = 1;
			}else {
				$wItem['stockquality'] = 0;
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
	 * @param Integer $aItems
	 * @param Integer $aNb
	 * @param Boolean $aRandom
	 * @return Array
	 */
	private function localSubsetPage($aItems,$aStartIdx,$aNb,$aRandom){

		$wItems = array();

		$wMax = count($aItems);
		if ($aNb +$aStartIdx>$wMax){
			$aNb=$wMax-$aStartIdx;
		}
		for ($i = $aStartIdx; $i < $aStartIdx+$aNb; $i++) {
			$wIdx = ($aRandom)? rand(0, $wMax-1): $i;
			array_push($wItems,$aItems[$wIdx]);
		}

		return $wItems;
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