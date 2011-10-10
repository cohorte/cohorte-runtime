<?php if(false){?>
<html>
<body>
<?php
}

$wQty = 1;

$wAddCartUrl =  base_url()."index.php/CHome/addToCart/".$id;

$wImageUrl = base_url()."app_resources/images/_items/".$id.".png";
?>
<div class="prod_box_big">
	<div class="top_prod_box_big"></div>
	<div class="center_prod_box_big">

		<div class="product_img_big">
			<a href="javascript:popImage('<?php echo $wImageUrl; ?>','Some Title')">
			<img
				src="<?php echo $wImageUrl; ?>"
				alt="" title="<?php echo $name; ?>" border="0" width="150px" /></a>
				
				
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
				<br /><br />
				Tip transport: <span class="blue">Mic</span>
				&nbsp;
				Tax include: <span class="blue">TVA</span>
				&nbsp;
				<span class="stock" title="<?php echo $stockQualityClass; ?>">Stock: <span class="stock<?php echo $stockQualityClass; ?>" style="font-size:12pt"><?php echo $stock; ?></span></span>
				
			</div>
			<div class="prod_price_big"> <span class="price"><?php echo $price; ?> EUR</span>
			</div>
			
			<?php if ($stock>0){ ?>
			<a href="<?php echo $wAddCartUrl;?>" class="addtocart">add to cart</a> 
			<?php }?>
			
			<a href="" class="compare">compare</a>
			
		</div>
	</div>
	<div class="bottom_prod_box_big"></div>
</div>

<?php if(false){?>
</body>
</html>
<?php }?>