<?php
class CHome extends MY_Controller {

	/**
	 *
	 * Enter description here ...
	 */
	public function __construct(){
		parent::__construct();
		log_message('debug', "** CHome.[init]");

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
	public function index(){
		
		log_message('debug', "** CHome.index()");

		$this->load->model('Item_model');


		$wCategorie = $this->pSessionData->getCategorie();
		$wDetailedItem = $this->pSessionData->getDetailedItem();
		$wPageBaseId = $this->pSessionData->getPageBaseId();

		$data['Categorie'] = $wCategorie;
		

		// get the 6 items of according the category
		$wItems = $this->Item_model->getItems($wCategorie,6,false,$wPageBaseId);
		
		// memo of the id of the last item of the page
		$this->pSessionData->setNextPageBaseId($wItems[count($wItems)-1]['id']);

		$data['Items'] = $this->injectStockInItems($wItems);


		// get the 3 random items in the categorie
		$wItemsRandom = $this->Item_model->getItems($wCategorie,2,true);

		$data['ItemsRandom'] = $this->injectStockInItems($wItemsRandom);
		
		// get the oferta item
		$data['ItemOferta'] = $this->Item_model->getItem('?');
		
		// get the special item
		$wItemSpecial = $this->Item_model->getItem('screen001');
		$data['ItemSpecial'] =$this->injectStockInItem($wItemSpecial);

		// get the new item
		$wItemNew = $this->Item_model->getItem('mouse001');
		$data['ItemNew'] =$this->injectStockInItem($wItemNew);

		if ($wDetailedItem != ''){
			$wItemDetail = $this->Item_model->getItem($wDetailedItem);
			if ($wItemDetail!=null){
				$data['ItemDetail'] =$this->injectStockInItem($wItemDetail);
			} else{
				//raz
				$wDetailedItem = '';
				$this->pSessionData->setDetailedItem($wDetailedItem);
			}
		}
		$data['DetailedItem'] = $wDetailedItem;
				
		
		$this->saveSessionData();
		
		$this->load->view('CHomeView',$data);
	}

	/**
	 * 
	 * @param String $aItemId
	 * @param String $aName
	 * @param Double $aPrice
	 * @param integer $aQty
	 */
	public function addToCart($aItemId=''){
		log_message('debug', "** CHome.addToCart() : wCartLine=[".$aItemId."]");
		
		if ($aItemId!=''){
		
			$this->load->model('Item_model');
			
			$wItem = $this->Item_model->getItem($aItemId);
			
			$wCartLine =  array(
	                       'id'      => $aItemId,
	                       'qty'     => 1,
	                       'price'   => $wItem['price'],
	                       'name'    => $wItem['name']
	                    );
			
			log_message('INFO', "** CHome.addToCart() : wCartLine=[".var_export($wCartLine,true)."]");
			
			$this->cart->insert($wCartLine);
		}
		$this->index();
	}
	
		
	/**
	*
	* Enter description here ...
	* @param unknown_type $aItemId
	*/
	public function showDetails($aItemId=''){
		
		log_message('debug', "** CHome.showDetails() : ItemId=[".$aItemId."]");
		
		if ($aItemId == 'x'){
			$aItemId = '?';
		}else if ($aItemId != ''){
			if (strpos($aItemId,' ')>-1){
				$aItemId='';
			}else{
				$aItemId = mb_strtolower($aItemId);
			}
		}
	
		$this->pSessionData->setDetailedItem($aItemId);
		// the sessin data is saved at the end of the "index" method.
		// $this->saveSessionData();			
		$this->index();
	}
	
	/**
	 *
	 * @param unknown_type $aCategorie
	 */
	public function changeCategorie($aCategorie){
		
		log_message('debug', "** CHome.changeCategorie() : Categorie=[".$aCategorie."]");
	
		$this->pSessionData->setCategorie($aCategorie);
		$this->pSessionData->setPreviousPageBaseId('');
		$this->pSessionData->setPageBaseId('');
		$this->pSessionData->setNextPageBaseId('');
		$this->pSessionData->setDetailedItem('');
		$this->saveSessionData();
	
		$this->index();
	}
	
	/**
	 *
	 * PreviousPageBaseId
	 * PageBaseId
	 * NextPageBaseId
	 * 
	 */
	public function previousPageItem(){
		
		$this->pSessionData->setPageBaseId($this->pSessionData->getPreviousPageBaseId());
		
		$this->pSessionData->setPreviousPageBaseId('');
		
		// the sessin data is saved at the end of the "index" method.
		// $this->saveSessionData();
		$this->index();
	}
	
	/**
	 *
	 * PreviousPageBaseId
	 * PageBaseId
	 * NextPageBaseId
	 * 
	 */
	public function nextPageItem(){
		
		$this->pSessionData->setPreviousPageBaseId($this->pSessionData->getPageBaseId());
		$this->pSessionData->setPageBaseId($this->pSessionData->getNextPageBaseId());
		
		// the sessin data is saved at the end of the "index" method.
		// $this->saveSessionData();	
		$this->index();
	}

}
?>