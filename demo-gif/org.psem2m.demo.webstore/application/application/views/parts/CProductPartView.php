<?php if(false){?>
<html>
<body>
<?php }

$wImageUrl = base_url()."app_resources/images/_items/".$id.".jpg";

$wDetailUrl = base_url()."index.php/CHome/showDetails/".$id; 

$wAddCartUrl =  base_url()."index.php/CHome/addToCart/".$id;

?>

<div class="prod_box">

	<div class="top_prod_box"></div>
	
	<div class="center_prod_box">
		<div class="product_title">
			<a href="<?php echo $wDetailUrl; ?>"><?php echo $id; ?> </a>
		</div>
		<div class="product_img">
			<a href="<?php echo $wDetailUrl; ?>"><img
				src="<?php echo $wImageUrl; ?>"
				alt="" title="<?php echo $name; ?>" border="0" width="100px"   height="100px"/> </a>
		</div>
		<div class="prod_price">
			<span class="price"><?php echo $price; ?> EUR</span>
			&nbsp;
			<span class="stock"  title="<?php echo $stockQualityClass; ?>">Stock: <span class="stock<?php echo $stockQualityClass; ?>"><?php echo $stock; ?></span></span>
			
		</div>
	</div>
	
	<div class="bottom_prod_box"></div>
	
	<div class="prod_details_tab">

		<?php if ($stock>0){ ?>
		<a href="<?php echo $wAddCartUrl; ?>" title="header=[Add to cart] body=[&nbsp;] fade=[on]"> <img
			src="<?php echo base_url(); ?>app_resources/images/cart.gif" alt=""
			title="" border="0" class="left_bt" /> </a> 
		<?php }?>
			
		<a href="#"
			title="header=[Specials] body=[&nbsp;] fade=[on]"> <img
			src="<?php echo base_url(); ?>app_resources/images/favs.gif" alt=""
			title="" border="0" class="left_bt" /> </a> 
			
			
		<a href="#"
			title="header=[Gifts] body=[&nbsp;] fade=[on]"> <img
			src="<?php echo base_url(); ?>app_resources/images/favorites.gif"
			alt="" title="" border="0" class="left_bt" /> </a> 
			
			
		<a href="<?php echo $wDetailUrl; ?>" class="prod_details">details</a>
		
	</div>
</div>
	
	
	
	



<?php if(false){?>
</body>
</html>

<?php }?>