<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');
/**
 * CodeIgniter extension
 *
 * An open source application development framework for PHP 5.1.6 or newer
 *
 * @package		CodeIgniter
 * @author		isandlaTech
 * @copyright	Copyright (c)  2011, isandlaTech.
 * @license		http://codeigniter.com/user_guide/license.html
 * @link		http://isandlaTech.com
 * @since		Version 1.0
 * @filesource
 */

// ------------------------------------------------------------------------


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
		
		$this->pSessionData = new CSessionData();
		$this->pSessionData->setSession($this->session);
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

	




}

?>