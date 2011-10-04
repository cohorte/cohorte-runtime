<?php
class CBean {

       private $prop =  null;
       
	/**
	 * Constructor
	 *
	 * initialize
	 */
       public function __construct($aArray = array()){
       		
       		$this->prop = $aArray;
       }

       public function __call( $name, $args ) {
       	
       			$value = (isset($args[0]))?$args[0]:"null";
       	
       			log_message('debug', "** CBean.__call() : [".$name."][".$value."]");
       	

               // Si nous avons getMaVariable
               if( $this->isGetter( $name ) ) {
                       // Retourne $this->prop[ 'ma_variable' ]
                       return $this->prop[ $this->toProperty( $name ) ];
               }
               // Si nous avons setMaVariable( $valeur )
               else if( $this->isSetter( $name ) ) {

               			$name = $this->toProperty( $name );
       					log_message('debug', "** CBean.__call() : store [".$name."]");
                       // Stocker la valeur dans $this->prop[ 'ma_variable']
                       return $this->prop[ $name] = $args[0];
               }

       }
       
       public function getProperties(){
      		return $this->prop;
       }

       private function isGetter( $name ) {
               return substr( $name, 0, 3 ) == 'get';
       }

       private function isSetter( $name ) {
               return substr( $name, 0, 3 ) == 'set';
       }

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