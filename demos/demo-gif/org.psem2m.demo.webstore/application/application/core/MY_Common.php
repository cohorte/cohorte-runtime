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



/**
 * Error Logging Interface
 *
 * return true if a logging level is enabled
 *
 * @access	public
 * @return	boolean
 */
if ( ! function_exists('log_isOn'))
{
	function log_isOn($aLevel = 'ERROR')
	{
		$wConfigLevel = config_item('log_threshold');

		if ($wConfigLevel == 0){
			return false;
		}
		
		// ATTENTION : this reference is deuplicated from "system/mibraries/Log.php"
		$wRefLevels	= array('ERROR' => '1', 'DEBUG' => '2',  'INFO' => '3', 'ALL' => '4');
		
		$aLevel = strtoupper($aLevel);
		
		if ( ! isset($wRefLevels[$aLevel]) OR ($wRefLevels[$aLevel] > $wConfigLevel)){
			return false;
		}
		
		return true;
	}
}
?>