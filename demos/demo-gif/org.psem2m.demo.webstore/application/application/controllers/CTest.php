<?php
class CTest extends MY_Controller {


	/**
	 * Constructor
	 *
	 * initialize
	 */
	public function __construct(){
		parent::__construct();
		log_message('debug', "** CTest.[init]");
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
		log_message('debug', "** CTest.index()");




		$wCategorie = $this->pSessionData->getCategorie();
		$wDetailedItem = $this->pSessionData->getDetailedItem();
		$wPageBaseId = $this->pSessionData->getPageBaseId();

		log_message('debug', "** CTest.index() wCategorie=[".$wCategorie."] wDetailedItem=[".$wDetailedItem."] wPageBaseId=[".$wPageBaseId."]");
		
		
		$data['SessionId']=$this->session->userdata('session_id');
		$data['Categorie'] = $wCategorie;
		$data['DetailedItem'] = $wDetailedItem;
		$data['PreviousPageBaseId'] = $this->pSessionData->getPreviousPageBaseId();
		$data['PageBaseId'] = $this->pSessionData->getPageBaseId();
		$data['NextPageBaseId'] = $this->pSessionData->getNextPageBaseId();
		
		
		$this->load->model('Item_model');
		$data['Items'] = $this->Item_model->getItems($wCategorie,6,false,$wPageBaseId);

		$this->pSessionData->setNbItems = 25;
		$this->pSessionData->setCategorie = ($wCategorie=="screens")?"mouses":"screens";

		$this->load->view('CTestView',$data);


	}
	public function newSession()
	{
		$this->session->sess_destroy();
		$this->session->sess_create();
				
		$this->index();
	}

}