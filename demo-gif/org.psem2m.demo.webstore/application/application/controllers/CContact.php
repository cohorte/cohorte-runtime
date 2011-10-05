<?php
class CContact extends MY_Controller {

	/**
	*
	* Enter description here ...
	*/
	public function __construct(){
		parent::__construct();
		log_message('debug', "** CContact.[init]");
	
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
		log_message('debug', "** CContact.index()");
		
		$this->load->model('Item_model');	
			
		$data = array();
		
		// get the oferta item
		$data['ItemOferta'] = $this->Item_model->getItem('?');
		
		// get the special item
		$wItemSpecial = $this->Item_model->getItem('screen020');
		$data['ItemSpecial'] =$this->injectStockInItem($wItemSpecial);
		
		// get the new item
		$wItemNew = $this->Item_model->getItem('mouse020');
		$data['ItemNew'] =$this->injectStockInItem($wItemNew);
		
		$this->load->view('CContactView',$data);
	}
	
	
}