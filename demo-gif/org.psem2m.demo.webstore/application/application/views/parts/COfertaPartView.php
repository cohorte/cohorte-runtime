<?php if(false){?>
<html>
<body>
<?php 
}

$wRandomImage= '_items/'.$id.'.png';

$wDetailUrl = "/". base_url()."index.php/CHome/showDetails/".$id;
?>
	<div class="oferte_content">
		<div class="top_divider">
			<img
				src="/<?php echo base_url(); ?>app_resources/images/header_divider.png"
				alt="" title="" width="1" height="164" />
		</div>
		<div class="oferta">

			<div class="oferta_content">
				<a href="<?php echo $wDetailUrl;?>" > <img src="/<?php echo base_url(); ?>app_resources/images/<?php echo $wRandomImage;?>"
					width="94" height="92" border="0" class="oferta_img" /></a>

				<div class="oferta_details">
					<div class="oferta_title"><?php echo $name;?></div>
					<div class="oferta_text"><?php echo $description;?></div>
					<a href="<?php echo $wDetailUrl;?>" class="details">details</a>
				</div>
			</div>


		</div>
		<div class="top_divider">
			<img
				src="/<?php echo base_url(); ?>app_resources/images/header_divider.png"
				alt="" title="" width="1" height="164" />
		</div>

	</div>

<?php if(false){?>
</body>
</html>
<?php }?>