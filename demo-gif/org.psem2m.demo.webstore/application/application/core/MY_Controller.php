<?php

class MY_Controller extends CI_Controller {

	/**
	 * The bean reprenting the data stored in the session
	 * 
	 * @var CSessionData
	 */
	protected $pSessionData = null;

	/**
	 * 
	 * Enter description here ...
	 */
	function __construct () {
		parent::__construct();
		log_message('debug', "** MY_Controller.[init]");
		
		$this->pSessionData = $this->retreiveSessionData();
	}

	/**
	 *
	 * Enter description here ...
	 * @param unknown_type $aItem
	 */
	protected function injectStockInItem($aItem){
		$wItemIds = array();
		array_push($wItemIds,$aItem['id']);
		$wItemsStock= $this->Item_model->getItemsStock($wItemIds);
		$aItem['stock']=$wItemsStock[0]['stock'];
		$aItem['stockQualityClass'] = $this->convertQualityToClass ($wItemsStock[0]['qualityLevel']);

		//echo  '<br/>'.var_export($aItem,true);

		return $aItem;
	}
	/**
	 *
	 * Enter description here ...
	 * @param Array  $aItems
	 */
	protected function injectStockInItems($aItems){

		//echo  '<br/>'.var_export($aItems,true);

		$wItemIds = array();
		foreach ($aItems as $wId=>$wItem) {
			array_push($wItemIds,$wItem['id']);
		}

		$wItemsStock= $this->Item_model->getItemsStock($wItemIds);

		//echo  '<br/>'.var_export($wItemsStock,true);

		$wItems = array();
		$wI=0;
		foreach ($aItems as $wId=>$wItem) {
			$wItem['stock'] = $wItemsStock[$wI]['stock'];
			$wItem['stockQualityClass'] = $this->convertQualityToClass ($wItemsStock[$wI]['qualityLevel']);
			//echo  '<br/>'.var_export($wItemsStock[$wI]['stock'],true)."&nbsp;".var_export($wItemsStock[$wI]['stockquality'],true);
			array_push($wItems,$wItem);
			$wI++;
		}

		//echo  '<br/>'.var_export($wItems,true);
		return $wItems;
	}
	/**
	 * 0 : SYNC Top qualité (retour direct de l’ERP)
	 * 1 : FRESH Cache niveau 1 (moins de 1 minute (incluses) depuis la mise en cache)
	 * 2 : ACCEPTABLE Cache niveau 2 (moins de 5 minutes (incluses) depuis la mise en cache)
	 * 3 : WARNING Cache niveau 3 (moins de 15 minutes (incluses) depuis la mise en cache)
	 * 4 : CRITICAL Qualité critique (plus de 15 minutes depuis la mise en cache)
	 *
	 * @param Integer $aQuality
	 */
	protected function convertQualityToClass($aQuality){
		if ($aQuality==0) return 'sync';
		if ($aQuality==1) return 'fresh';
		if ($aQuality==2) return 'acceptable';
		if ($aQuality==3) return 'warning';
		return 'critical';
	}

	


	/**
		*
		* @return    the current instance of CSessionData
		*/
	protected function getSessionData(){
		log_message('debug', "** MY_Controller.getSessionData()");

		if ($this->pSessionData==null){
			$this->pSessionData = retreiveSessionData();
		}

		return $this->pSessionData;
	}

	/**
	 *
		* Save the current instance of CSessionData
		*/
	protected function saveSessionData(){
		log_message('debug', "** MY_Controller.saveSessionData()");

		$this->storeSessionData($this->pSessionData);
	}
	/**
		* retrieve the instance of CSessionData in the CI session. Creates a new one if it doesn't exist.
		*
		* @return    a instance of CSessionData
		**/
	protected function retreiveSessionData(){
		log_message('debug', "** MY_Controller.retreiveSessionData()");

		$wElectronix = $this->session->userdata('electronix');

		log_message('debug', "** MY_Controller.retreiveSessionData() : Electronix=[".$wElectronix ."]" );

		if ($wElectronix == false){
			log_message('debug', "** MY_Controller.retreiveSessionData() : no GifData ! ");

			$wSessionData = $this->storeSessionData($this->newSessionData());
		}else {
			// Creates the SessionData bean with the array "all_userdata"
			$wArray = $this->session->all_userdata();
			if (array_key_exists('cart_contents', $wArray)) {
				
				$wArray2 = array();
				foreach($wArray as $wKey=>$wValue){
					log_message('INFO', "** MY_Controller.retreiveSessionData() :  wKey=[". var_export($wKey,true)."] wValue=[". var_export($wValue,true)."]" );
						
					if ($wKey != 'cart_contents'){
						$wArray2[$wKey]=$wValue;
					}
				}
				
					
				$wArray = $wArray2;
			}
			$wSessionData = new CSessionData( $wArray);
			log_message('INFO', "** MY_Controller.retreiveSessionData() : wSessionData=[". var_export($wSessionData,true)."]" );
				
		}
		return $wSessionData;
	}
	
	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aArray
	 * @param unknown_type $aKey
	 * @return multitype:|unknown
	 */
	function removeFromArray($aArray, $aKey){
		$wIdx=0;
		foreach($aArray as $wKey=>$wValue){
			if($wKey == $aKey){
				return array_splice($aArray, $wIdx, 1);
			}
			$wIdx++;
		}
		return $aArray;
	}
	/**
		*
		* @return    a new instance of CSessionData
		**/
	private function newSessionData(){
		log_message('debug', "** MY_Controller.newSessionData()");
		$wSessionData = new CSessionData();
		$wSessionData->setElectronix($wSessionData->getTimeStamp());
		$wSessionData->setCategorie('screens');
		
		$wSessionData->setDetailedItem('');
		
		$wSessionData->setPreviousPageBaseId('');
		$wSessionData->setPageBaseId('');
		$wSessionData->setNextPageBaseId('');

		return $wSessionData;
	}

	/**
		*
		* @param    $aSessionData    the session data to store
		* @return    the stored instance of CSessionData
		**/
	protected function storeSessionData($aSessionData){
		log_message('debug', "** MY_Controller.storeSessionData()");
		$this->session->set_userdata($aSessionData->getProperties());

		log_message('debug', "** MY_Controller.storeSessionData() : all_userdata=[". var_export($this->session->all_userdata(),true)."]" );
		return $aSessionData;
	}

}

?>