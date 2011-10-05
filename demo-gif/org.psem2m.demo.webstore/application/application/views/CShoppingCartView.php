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
<?php $this->load->view('parts/COfertaPartView',$ItemOferta); ?>

</div>
    
  <div id="main_content"> 

<!--  LOAD MENUBAR -->
<?php $this->load->view('parts/CMenuBarPartView'); ?>
            
<!--  LOAD NAVIGATOR -->
<?php 
$datanav['pagePath']='Home/Shopping cart';
$this->load->view('parts/CNavigatorPartView',$datanav); 
?>           
    
    
 <div class="left_content">

<!--  LOAD CATEGORIES -->
<?php $this->load->view('parts/CCategoriesPartView'); ?>
        
<!--  LOAD SPECIAL PRODUCT -->
<div class="title_box">Special Products</div>
<?php $this->load->view('parts/CProductHightLightPartView',$ItemSpecial); ?>
     
<!--  LOAD NEWSLETTER -->
<?php $this->load->view('parts/CNewsLetterPartView'); ?>

<!--  LOAD ADDS -->
<?php $this->load->view('parts/CAddsPartView'); ?>  
        
    
</div><!-- end of left content -->
  
   
<div class="center_content">
<div class="center_title_bar">Shopping cart</div>
    
<div class="prod_box_big">
<div class="top_prod_box_big"></div>
<div class="center_prod_box_big">            
                 
<div class="contact_form">
                           
<div class="form_row">

<?php 

echo form_open('updateCart');
?>

<table id="shoppingcart"  >

<tr>
  <th>QTY</th>
  <th>Item Description</th>
  <th style="text-align:right">Item Price</th>
  <th style="text-align:right">Sub-Total</th>
</tr>

<?php 

$i = 1; 

foreach ( $this->cart->contents() as $items): 

echo form_hidden($i.'[rowid]', $items['rowid']); ?>

	<tr>
	  <td><?php echo form_input(array('name' => $i.'[qty]', 'value' => $items['qty'], 'maxlength' => '3', 'size' => '5')); ?></td>
	  <td>
		<?php echo $items['name']; ?>

			<?php if ($this->cart->has_options($items['rowid']) == TRUE): ?>

				<p>
					<?php foreach ($this->cart->product_options($items['rowid']) as $option_name => $option_value): ?>

						<strong><?php echo $option_name; ?>:</strong> <?php echo $option_value; ?><br />

					<?php endforeach; ?>
				</p>

			<?php endif; ?>

	  </td>
	  <td style="text-align:right"><?php echo $this->cart->format_number($items['price']); ?></td>
	  <td style="text-align:right"><?php echo $this->cart->format_number($items['subtotal']); ?></td>
	</tr>

<?php 
$i++; 
endforeach; 
?>

<tr>
  <td colspan="2"> </td>
  <td class="right"><strong>Total</strong></td>
  <td class="right">$<?php echo $this->cart->format_number($this->cart->total()); ?></td>
</tr>

</table>

<script>
function callControler(aMethod){
	var wUrl = "/<?php echo base_url(); ?>index.php/CShoppingCart/"+aMethod;
	window.location.href=wUrl;
}

</script>

<p>
<?php echo form_submit('', 'Update your Cart'); ?>

<input type="button" onclick="callControler('eraseCart');" value="Erase cart"/>

<input type="button" onclick="callControler('sendCart');" value="Send cart"/>
</p>

</div>  

</div> 
                
                                     
</div>
<div class="bottom_prod_box_big"></div>                                
</div>
       
    
   
</div><!-- end of center content -->
   
<div class="right_content">

     
<!--  LOAD NEWSLETTER -->
<?php $this->load->view('parts/CShoppingCartSummaryPartView'); ?>


<!--  LOAD PRODUCT NEW -->
<div class="title_box">What's new</div>
<?php $this->load->view('parts/CProductHightLightPartView',$ItemNew); ?>


<!--  LOAD NEWSLETTER -->
<?php $this->load->view('parts/CManufacturersPartView'); ?>  
     
<!--  LOAD ADDS -->
<?php $this->load->view('parts/CAddsPartView'); ?>   
     
</div><!-- end of right content -->   
   
            
</div><!-- end of main content -->
   
<!--  LOAD FOOTER -->
<?php $this->load->view('parts/CFooterPartView'); ?>              


</div>
<!-- end of main_container -->
</body>
</html>
