<?php if(false){?>
<html>
<body>
<?php
}

$wQty = 1;

$wAddCartUrl = "/". base_url()."index.php/CHome/addToCart/".$id;

$wImageUrl = "/".base_url()."app_resources/images/_items/".$id.".png";
?>

	<div class="prod_box_big">
		<div class="top_prod_box_big"></div>
		<div class="center_prod_box_big">

			<div class="product_img_big">
				<a href="javascript:popImage('<?php echo $wImageUrl; ?>','Some Title')"
					title="header=[Zoom] body=[&nbsp;] fade=[on]"><img
					src="<?php echo $wImageUrl; ?>"
					alt="" title="" border="0" width="150px" /> </a>
					
					
				<div class="thumbs">
					<a href="" title="header=[Thumb1] body=[&nbsp;] fade=[on]"><img
						src="<?php echo $wImageUrl; ?>"
						alt="" title="" border="0" width="25px" /> </a> 
				</div>
			</div>
			<div class="details_big_box">
				<div class="product_title_big"><?php echo $id; ?> </div>
				<div class="specifications">
					Product : <span class="blue"><?php echo $name; ?></span>
					<br />
					Description: <span class="blue"><?php echo $description; ?></span>
					<br />
					Availability: <span class="prod_price_big"> <span class="stock<?php echo $stockQualityClass; ?>"><?php $stock; ?></span></span>
					<br />
					Tip transport: <span class="blue">Mic</span>
					<br />
					Tax include: <span class="blue">TVA</span><br />
				</div>
				<div class="prod_price_big"> <span class="price"><?php echo $price; ?> EUR</span>
				</div>

				<a href="<?php echo $wAddCartUrl;?>" class="addtocart">add to cart</a> <a href=""
					class="compare">compare</a>
			</div>
		</div>
		<div class="bottom_prod_box_big"></div>
	</div>
	
	


<?php if(false){?>
</body>
</html>
<?php }?>