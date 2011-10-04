<?php
class CShoppingCart extends CI_Controller {

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
		log_message('debug', "** CShoppingCart.index()");
		
		$this->load->model('Item_model');	
			
		$data = array();
		
// 		$data['ItemSpecial'] = $this->Item_model->getItem('screen001');
		
// 		$data['ItemNew'] = $this->Item_model->getItem('mouse001');

		
		// get the special item
		$wItemSpecial = $this->Item_model->getItem('screen012');
		$data['ItemSpecial'] =$this->injectStockInItem($wItemSpecial);
		
		// get the new item
		$wItemNew = $this->Item_model->getItem('mouse004');
		$data['ItemNew'] =$this->injectStockInItem($wItemNew);
		
		
		$this->load->view('CShoppingCartView',$data);
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
		$aItem['stockclass'] = $this->convertQualityToClass ($wItemsStock[0]['stockquality']);
	
		//echo  '<br/>'.var_export($aItem,true);
	
		return $aItem;
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
}