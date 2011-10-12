<?php
class CSessionData   {

	private $pSession;

	/**
	 * Explicit public constructor used when the library is loaded
	 *
	 * initialize
	 */
	public function __construct(){
		//
	}

	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $aSession
	 */
	public function setSession($aSession){
		$this->pSession = $aSession;
		$this->validateSession();
	}

	/**
	 *
	 * Enter description here ...
	 */
	private function validateSession(){
		if(log_isOn('debug')){
			log_message('debug', "** CSessionData.validateSession()");
		}

		if (! $this->pSession->userdata('electronix'))
			$this->setElectronix(microtime());

		if (! $this->pSession->userdata('categorie'))
			$this->setCategorie('screens');

		if (! $this->pSession->userdata('detailed_item'))
			$this->setDetailedItem('');

		if (! $this->pSession->userdata('previous_page_base_id'))
			$this->setPreviousPageBaseId('');

		if (! $this->pSession->userdata('page_base_id'))
			$this->setPageBaseId('');

		if (! $this->pSession->userdata('next_page_base_id'))
			$this->setNextPageBaseId('');
	}

	/**
	 * 
	 * Enter description here ...
	 */
	public function dump(){
		if(log_isOn('INFO')){
			log_message('INFO', "** CSessionData.dump() : SessionData=[". var_export($this->pSession->all_userdata(),true)."]" );
		}
	}

	/**
	 * 
	 * Enter description here ...
	 * @param unknown_type $name
	 * @param unknown_type $args
	 */
	public function __call( $name, $args ) {

		$value = (isset($args[0]))?$args[0]:"no value";

		if(log_isOn('DEBUG')){
			log_message('DEBUG', "** CSessionData.__call() : [".$name."]");
		}

		// Si nous avons getMaVariable
		if( $this->isGetter( $name ) ) {

			$name = $this->toProperty( $name );
			$value = $this->pSession->userdata($name);
			
			if(log_isOn('INFO')){
				log_message('INFO', "** CSessionData.__call() : read [".$name."]=>[".$value."]");
			}
			return $value;
		}
		// Si nous avons setMaVariable( $valeur )
		else if( $this->isSetter( $name ) ) {

			$name = $this->toProperty( $name );
			if(log_isOn('INFO')){
				log_message('INFO', "** CSessionData.__call() : store [".$name."]<=[".$value."]");
			}
			return $this->pSession->set_userdata( $name , $args[0] );
		}

	}

	/**
	 * 
	 * @param String $name
	 * @return boolean
	 */
	private function isGetter( $name ) {
		return substr( $name, 0, 3 ) == 'get';
	}

	/**
	 * 
	 * @param String $name
	 * @return boolean
	 */
	private function isSetter( $name ) {
		return substr( $name, 0, 3 ) == 'set';
	}

	/**
	 * 
	 * @param String $name
	 * @return String
	 */
	private function toProperty( $name ) {

		// Transforme setFirstName en set_First_Name
		$str = preg_replace( '/[A-Z]/', '_$0', $name );

		// Transforme set_First_Name en set_first_name
		$str = strtolower( $str );

		// Transforme set_first_name en first_name
		$str = substr( $str, 4 );
			
		return $str;
	}
}
?>