<?php
class CXml {

	private $document;

	function __construct () {
		log_message('debug', "** CXml.[init]");
	}

	/**
	 * to be sure we read UTF-8 xml content
	 * 
	 * @see http://de3.php.net/manual/en/function.file-get-contents.php#85008
	 * 
	 * @param String $file
	 * @return string
	 */
	private function file_get_contents_utf8($file) {
		$content = file_get_contents($file);
		/* 
		 "auto" may be used, which expands to  "ASCII,JIS,UTF-8,EUC-JP,SJIS".
		 */
		return mb_convert_encoding($content, 'UTF-8','auto');
	}
	
	public function load($file) {
		log_message('debug', "** CXml.load() : file=[".$file."]");
		
		if (! file_exists($file)) return false;
		return $this->document = ($this->file_get_contents_utf8($file));
	}

	public function parse() {

		$xml = $this->document;
		if ($xml == '') return false;

		$doc = new DOMDocument ();
		$doc->preserveWhiteSpace = false;
		if ($doc->loadXML($xml)) {
			$array = $this->flatten_node($doc);
			if (count ($array) > 0) return $array;
		}
		return false;

	}

	private function get_attrs($child, $value) {

		if ($child->hasAttributes()) {
			$attrs = array();

			foreach ($child->attributes as $attribute) {
				$attrs[$attribute->name] = $attribute->value;
			}
			return array('__value'=>$value, '__attrs'=>$attrs);
		}
		return $value;

	}
	
	
	private function flatten_node($node) {

		$array = array();

		foreach ($node->childNodes as $child) {

			if ($child->hasChildNodes()) {
				$array[$child->nodeName][] = $this->get_attrs($child, $this->flatten_node($child));

			} elseif ($child->nodeValue == '') {
				$array[$child->nodeName][] = $this->get_attrs($child, '');

			} else {
				return $child->nodeValue;
			}
		}
		return $array;

	}

}
?>