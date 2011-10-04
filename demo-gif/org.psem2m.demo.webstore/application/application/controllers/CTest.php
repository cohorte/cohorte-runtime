<?php
class CTest extends CI_Controller {

	private $pSessionData = null;

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

		$this->session->sess_create();
		$this->pSessionData = $this->retreiveSessionData();


		$wCategorie = $this->pSessionData->getCategorie();
		$wDetailedItem = $this->pSessionData->getDetailedItem();

		$data['SessionId']=$this->session->userdata('session_id');
		$data['Categorie'] = $wCategorie;
		$data['DetailedItem'] = $wDetailedItem;
		
		
		$this->load->model('Item_model');
		$data['Items'] = $this->Item_model->getItems($wCategorie,0,12,false);

		$this->pSessionData->setNbItems = 25;
		$this->pSessionData->setCategorie = ($wCategorie=="screens")?"mouses":"screens";
		$this->saveSessionData();

		$this->load->view('CTestView',$data);


	}


	// ******************************************************************************************
	// A set of méthod to put in the MY_CI_Controller classe when it will work...
	// ******************************************************************************************
	
	/**
	 *
	 * @return    the current instance of CSessionData
	 */
	protected function getSessionData(){
		log_message('debug', "** CTest.getSessionData()");

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
		log_message('debug', "** CTest.saveSessionData()");

		$this->storeSessionData($this->pSessionData);
	}
	/**
	 * retrieve the instance of CSessionData in the CI session. Creates a new one if it doesn't exist.
	 *
	 * @return    a instance of CSessionData
	 **/
	protected function retreiveSessionData(){
		log_message('debug', "** CTest.retreiveSessionData()");

		$wElectronix = $this->session->userdata('electronix');

		log_message('debug', "** CTest.retreiveSessionData() : Electronix=[".$wElectronix ."]" );

		if ($wElectronix == false){
			log_message('debug', "** CTest.retreiveSessionData() : no GifData ! ");

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
		log_message('debug', "** CTest.newSessionData()");
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
		log_message('debug', "** CTest.storeSessionData()");
		$this->session->set_userdata($aSessionData->getProperties());

		log_message('debug', "** CTest.storeSessionData() : all_userdata=[". var_export($this->session->all_userdata(),true)."]" );
		return $aSessionData;
	}

}