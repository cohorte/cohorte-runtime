<?php
class CHome extends CI_Controller {



	private $pSessionData = null;

	public function __construct(){
		parent::__construct();
		log_message('debug', "** CHome.[init]");
		
		$this->pSessionData = $this->retreiveSessionData();
		
	}

	/**
	 * Index Page for this controller.
	 *
	 * Maps to the following URL
	 * 		http://example.com/index.php/welcome
	 *	- or -
	 * 		http://example.com/index.php/welcome/index
	 *	- or -
	 * Since this controller is set as the default controller in
	 * config/routes.php, it's displayed at http://example.com/
	 *
	 * So any other public methods not prefixed with an underscore will
	 * map to /index.php/welcome/<method_name>
	 * @see http://codeigniter.com/user_guide/general/urls.html
	 */
	public function index()
	{
		log_message('debug', "** CHome.index()");

		$this->load->model('Item_model');


		$wRandomIdx = rand(1, 21);
		$wRandomIdx = str_pad($wRandomIdx, 3, "0", STR_PAD_LEFT);
		$wRandomTyp = rand(0, 1);
		$wRandomItem = (($wRandomTyp==0)?'screen':'mouse').$wRandomIdx;


		$wCategorie = $this->pSessionData->getCategorie();
		$wDetailedItem = $this->pSessionData->getDetailedItem();
		$wStartPageIdx = $this->pSessionData->getStartPageIdx();
		
		$data['Categorie'] = $wCategorie;
		$data['DetailedItem'] = $wDetailedItem;
		
		// get the 6 first items of a categorie
		$wItems = $this->Item_model->getItems($wCategorie,$wStartPageIdx,6,false);
		
		$data['Items'] = $this->injectStockInItems($wItems);
		
		
		// get the 3 random items in the categorie
		$wItemsRandom = $this->Item_model->getItems($wCategorie,0,2,true);

		$data['ItemsRandom'] = $this->injectStockInItems($wItemsRandom);
		
		// get the special item
		$wItemSpecial = $this->Item_model->getItem('screen001');
		$data['ItemSpecial'] =$this->injectStockInItem($wItemSpecial);
		
		// get the new item
		$wItemNew = $this->Item_model->getItem('mouse001');
		$data['ItemNew'] =$this->injectStockInItem($wItemNew);
		
		if ($wDetailedItem != ''){
			$wItemDetail = $this->Item_model->getItem($wDetailedItem);
			$data['ItemDetail'] =$this->injectStockInItem($wItemDetail);
				
		}

		$this->load->view('CHomeView',$data);
	}
	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aItem
	 */
	private function injectStockInItem($aItem){
		$wItemIds = array();
		array_push($wItemIds,$aItem['id']);
		$wItemsStock= $this->Item_model->getItemsStock($wItemIds);
		$aItem['stock']=$wItemsStock[0]['stock'];
		$aItem['qualityClass'] = $this->convertQualityToClass ($wItemsStock[0]['qualityLevel']);
		
		//echo  '<br/>'.var_export($aItem,true);
		
		return $aItem;
	}
	/**
	 * 
	 * Enter description here ...
	 * @param Array  $aItems
	 */
	private function injectStockInItems($aItems){
		
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
			$wItem['qualityClass'] = $this->convertQualityToClass ($wItemsStock[$wI]['qualityLevel']);
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
	private function convertQualityToClass($aQuality){
		if ($aQuality==0) return 'sync';
		if ($aQuality==1) return 'fresh';
		if ($aQuality==2) return 'acceptable';
		if ($aQuality==3) return 'warning';
		return 'critical';
	}
	
	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aItemId
	 */
	public function showDetails($aItemId='')
	{
		log_message('debug', "** CHome.showDetails() : ItemId=[".$aItemId."]");
				
		
		$this->pSessionData->setDetailedItem($aItemId);
		$this->saveSessionData();
		
		$this->index();
	}

	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aCategorie
	 */
	public function changeCategorie($aCategorie)
	{
		log_message('debug', "** CHome.changeCategorie() : Categorie=[".$aCategorie."]");

		$this->pSessionData->setCategorie($aCategorie);
		$this->pSessionData->setStartPageIdx(0);
		$this->pSessionData->setDetailedItem('');
		$this->saveSessionData();
		
		$this->index();
	}


	/**
	 * 
	 * Enter description here ...
	 */
	public function previousPageItem(){
		
		$wStartPageIdx = $this->pSessionData->getStartPageIdx();
		
		$wStartPageIdx = $wStartPageIdx-6;
		if ($wStartPageIdx<0) $wStartPageIdx=0;
		
		$this->pSessionData->setStartPageIdx($wStartPageIdx);
		$this->saveSessionData();
		
		$this->index();
	}
	
	/**
	 * 
	 * Enter description here ...
	 */
	public function nextPageItem(){
	
		$wStartPageIdx = $this->pSessionData->getStartPageIdx();
	
		$wStartPageIdx = $wStartPageIdx+6;
		if ($wStartPageIdx<0) $wStartPageIdx=0;
	
		$this->pSessionData->setStartPageIdx($wStartPageIdx);
		$this->saveSessionData();
	
		$this->index();
	}


	// ******************************************************************************************
	// A set of méthod to put in the MY_CI_Controller classe when it will work...
	// ******************************************************************************************

	/**
	 *
	 * @return    the current instance of CSessionData
	 */
	protected function getSessionData(){
		log_message('debug', "** MY_CI_Controller.getSessionData()");

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
		log_message('debug', "** MY_CI_Controller.saveSessionData()");

		$this->storeSessionData($this->pSessionData);
	}
	/**
	 * retrieve the instance of CSessionData in the CI session. Creates a new one if it doesn't exist.
	 *
	 * @return    a instance of CSessionData
	 **/
	protected function retreiveSessionData(){
		log_message('debug', "** MY_CI_Controller.retreiveSessionData()");

		$wElectronix = $this->session->userdata('electronix');

		log_message('debug', "** MY_CI_Controller.retreiveSessionData() : Electronix=[".$wElectronix ."]" );

		if ($wElectronix == false){
			log_message('debug', "** MY_CI_Controller.retreiveSessionData() : no GifData ! ");

			$wSessionData = $this->storeSessionData($this->newSessionData());
		}else {
			// Creates the SessionData bean with the array "all_userdata"
			$wSessionData = new CSessionData($this->session->all_userdata());
		}
		return $wSessionData;
	}

	/**
	 *
	 * @return    a new instance of CSessionData
	 **/
	private function newSessionData(){
		log_message('debug', "** MY_CI_Controller.newSessionData()");
		$wSessionData = new CSessionData();
		$wSessionData->setElectronix($wSessionData->getTimeStamp());
		$wSessionData->setCategorie('screens');
		$wSessionData->setDetailedItem('');
		$wSessionData->setStartPageIdx(0);

		return $wSessionData;
	}

	/**
	 *
	 * @param    $aSessionData    the session data to store
	 * @return    the stored instance of CSessionData
	 **/
	protected function storeSessionData($aSessionData){
		log_message('debug', "** MY_CI_Controller.storeSessionData()");
		$this->session->set_userdata($aSessionData->getProperties());

		log_message('debug', "** MY_CI_Controller.storeSessionData() : all_userdata=[". var_export($this->session->all_userdata(),true)."]" );
		return $aSessionData;
	}


}
?>