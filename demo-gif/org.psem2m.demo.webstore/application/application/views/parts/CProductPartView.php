<?php if(false){?>
<html>
<body>
<?php }

$wImageUrl = "/".base_url()."app_resources/images/_items/".$Item['id'].".jpg";

$wDetailUrl = "/". base_url()."/index.php/CHome/showDetails/".$Item['id']; 

?>

<div class="prod_box">

	<div class="top_prod_box"></div>
	
	<div class="center_prod_box">
		<div class="product_title">
			<a href="<?php echo $wDetailUrl; ?>"><?php echo $Item['id']; ?> </a>
		</div>
		<div class="product_img">
			<a href="<?php echo $wDetailUrl; ?>"><img
				src="<?php echo $wImageUrl; ?>"
				alt="" title="" border="0" width="100px" /> </a>
		</div>
		<div class="prod_price">
			<span class="price"><?php echo $Item['price']; ?> EUR</span>
			&nbsp;
			<span class="stock">Availability: <span class="stock<?php echo $Item['stockclass']; ?>"><?php echo $Item['stock']; ?></span></span>
			
		</div>
	</div>
	
	<div class="bottom_prod_box"></div>
	
	<div class="prod_details_tab">

		<a href="#" title="header=[Add to cart] body=[&nbsp;] fade=[on]"> <img
			src="/<?php echo base_url(); ?>app_resources/images/cart.gif" alt=""
			title="" border="0" class="left_bt" /> </a> 

			
			<a href="#"
			title="header=[Specials] body=[&nbsp;] fade=[on]"> <img
			src="/<?php echo base_url(); ?>app_resources/images/favs.gif" alt=""
			title="" border="0" class="left_bt" /> </a> 
			
			
			<a href="#"
			title="header=[Gifts] body=[&nbsp;] fade=[on]"> <img
			src="/<?php echo base_url(); ?>app_resources/images/favorites.gif"
			alt="" title="" border="0" class="left_bt" /> </a> 
			
			
			<a href="<?php echo $wDetailUrl; ?>" class="prod_details">details</a>
	</div>
</div>
	
	
	
	



<?php if(false){?>
</body>
</html>

<?php }?>