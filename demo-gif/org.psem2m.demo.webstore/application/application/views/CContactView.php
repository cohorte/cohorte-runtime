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
$datanav['pagePath']='Home/Contact';
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
        
    
</div><!-- end of left content -->
  
   
<div class="center_content">
<div class="center_title_bar">Contact Us</div>
    
<div class="prod_box_big">
<div class="top_prod_box_big"></div>
<div class="center_prod_box_big">            
                 
<div class="contact_form">
                           
<div class="form_row">
<label class="contact"><strong>Name:</strong></label>
<input type="text" class="contact_input" />
</div>  

<div class="form_row">
<label class="contact"><strong>Email:</strong></label>
<input type="text" class="contact_input" />
</div>

<div class="form_row">
<label class="contact"><strong>Phone:</strong></label>
<input type="text" class="contact_input" />
</div>
                    
<div class="form_row">
<label class="contact"><strong>Company:</strong></label>
<input type="text" class="contact_input" />
</div>


<div class="form_row">
<label class="contact"><strong>Message:</strong></label>
<textarea class="contact_textarea" ></textarea>
</div>

                    
<div class="form_row">
<a href="#" class="contact">send</a>
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
<?php 
$wDataNew = array();
$wDataNew['Item']= $ItemNew;
$this->load->view('parts/CProductHightLightPartView',$wDataNew); ?>


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
