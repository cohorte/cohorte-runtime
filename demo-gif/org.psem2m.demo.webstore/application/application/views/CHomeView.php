<?php if(false){?>
<html>
<body>
<?php }?>



<!--  LOAD HEADER -->
<?php $this->load->view('parts/CHeaderPartView'); ?>

<div id="main_container">

<!--  LOAD TOPBAR -->
<?php $this->load->view('parts/CTopBarPartView'); ?>

<div id="header">

<!--  LOAD LOGO -->
<?php $this->load->view('parts/CLogoPartView'); ?>

<!--  LOAD OFERTA -->
<?php $this->load->view('parts/COfertaPartView'); ?>

</div>

<div id="main_content">

<!--  LOAD MENUBAR -->
<?php $this->load->view('parts/CMenuBarPartView'); ?>

<!--  LOAD NAVIGATOR -->
<?php 
$datanav['pagePath']='Home';
$this->load->view('parts/CNavigatorPartView',$datanav); 
?>


<div class="left_content">

<!--  LOAD CATEGORIES -->
<?php $this->load->view('parts/CCategoriesPartView'); ?>


<!--  LOAD SPECIAL PRODUCT -->
<div class="title_box">Special Products</div>
<?php 
$wDataSpecial = array();
$wDataSpecial['Item']= $ItemSpecial;
$this->load->view('parts/CProductHightLightPartView',$wDataSpecial); ?>

     
			<!--  LOAD NEWSLETTER -->
<?php $this->load->view('parts/CNewsLetterPartView'); ?>


<!--  LOAD ADDS -->
<?php $this->load->view('parts/CAddsPartView'); ?>


</div>
<!-- end of left content -->


<div class="center_content">

<?php if ($DetailedItem == "") {  ?>

<div class="center_title_bar">Special <?php echo $Categorie ?></div>

<!--  LOAD SPECIAL PRODUCTS -->
<?php foreach ($ItemsRandom as $Id1=>$wItemR) {
		$wDataProduct = array();
		$wDataProduct['Item']=$wItemR;
		$wDataProduct['Categorie'] = $Categorie;
		$this->load->view('parts/CProductPartView',$wDataProduct);
	}
?>

<?php }else{ ?>

<div class="center_title_bar"><?php echo $DetailedItem ?> details ... &nbsp;  &nbsp;<span style="font-size:9pt;font-weight:normal;">(  &nbsp;

 <a href="/<?php echo base_url(); ?>index.php/CHome/showDetails">close</a>
 &nbsp;)</span>
</div>

<!--  LOAD PRODUCT DETAILS -->
<?php 
				
	$wDataProductDetail = array();
	$wDataProductDetail['Item']=$ItemDetail;
	$wDataProductDetail['Categorie'] = $Categorie;
	$this->load->view('parts/CProductDetailPartView',$wDataProductDetail);

?>
<?php }?>
				
<div class="center_title_bar">
<?php echo $Categorie ?> &nbsp;  &nbsp;<span style="font-size:9pt;font-weight:normal;">(  &nbsp;
<a href="/<?php echo base_url(); ?>index.php/CHome/previousPageItem">previous</a>
 &nbsp;
 <a href="/<?php echo base_url(); ?>index.php/CHome/nextPageItem">next</a>
 &nbsp;)</span>
</div>
				
<!--  LOAD PRODUCT PAGE -->
<?php foreach ($Items as $Id1=>$wItemL) {
				
	
						
	$wDataProduct = array();
	$wDataProduct['Item']=$wItemL;
	$wDataProduct['Categorie'] = $Categorie;
	$this->load->view('parts/CProductPartView',$wDataProduct);
}
?>


</div>
<!-- end of center content -->

<div class="right_content">

     
<!--  LOAD NEWSLETTER -->
<?php $this->load->view('parts/CShoppingCartSummaryPartView'); ?>


<!--  LOAD PRODUCT NEW -->
<div class="title_box">What's new</div>
<?php 
$wDataNew = array();
$wDataNew['Item']= $ItemNew;
$this->load->view('parts/CProductHightLightPartView',$wDataNew); ?>


<!--  LOAD NEWSLETTER -->
<?php $this->load->view('parts/CManufacturersPartView'); ?>

<!--  LOAD ADDS -->
<?php $this->load->view('parts/CAddsPartView'); ?>


</div>
<!-- end of right content -->


</div>
<!-- end of main content -->

<!--  LOAD FOOTER -->
<?php $this->load->view('parts/CFooterPartView'); ?>

</div>
<!-- end of main_container -->
</body>
</html>
