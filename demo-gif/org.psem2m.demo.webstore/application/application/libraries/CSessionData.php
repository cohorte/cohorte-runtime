<?php
class CSessionData  extends CBean {

	private  $pTimeStamp = 0;
	
	/**
	* Constructor
	*
	* initialize
	*/
	public function __construct($aArray = array()){
		parent::__construct($aArray);
		
		$this->pTimeStamp = microtime();
	}
	
	public function getTimeStamp(){
		return $this->pTimeStamp;
	}

	
}

?>