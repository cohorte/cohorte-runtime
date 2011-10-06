<?php ?>
<!DOCTYPE html>
<html lang="en">
<head>
<style>
table td {
	border: 1px solid black;
}

td {
	border: 1px solid red;
}
</style>
</head>
<body>

	<table>
		<tr>
			<td>echo $SessionId</td>
			<td><?php echo $SessionId ?></td>
		</tr>
		<tr>
			<td>echo print_r($this-&gt;session-&gt;userdata);</td>
			<td><?php echo print_r($this->session->userdata); ?></td>
		</tr>

		<tr>
			<td>echo site_url("news/local/123");</td>
			<td><?php echo site_url("news/local/123"); ?></td>
		</tr>
		<tr>
			<td>echo base_url();</td>
			<td><?php echo base_url(); ?></td>
		</tr>
		<tr>
			<td>echo base_url("blog/post/123");</td>
			<td><?php echo base_url("blog/post/123"); ?></td>
		</tr>

		<tr>
			<td>echo session_id();</td>
			<td><?php echo session_id(); ?></td>
		</tr>

		<tr>
			<td>echo $Categorie</td>
			<td><?php echo $Categorie ?></td>
		</tr>
		<tr>
			<td>echo $DetailedItem</td>
			<td><?php echo $DetailedItem ?></td>
		</tr>

	</table>

	<p>
		<a href="<?php echo base_url(); ?>index.php/CHome" class="nav1">Home </a>
		&nbsp;
		<a href="<?php echo base_url(); ?>index.php/CTest" class="nav1">Test </a>
	</p>

<p>
<?php 
try {
	if ( ! function_exists('log_isOn'))
	{
		echo "function_exists('log_isOn') => false";
	}else{
		if (log_isOn('DEBUG')){
				echo "log_isOn('DEBUG') => true";
		}else{
			echo "log_isOn('DEBUG') => false";
		}
	}
} catch (Exception $e) {
    echo 'Exception reçue : ',  $e->getMessage();
}
?>
</p>


	<table >
	<?php foreach($Items as $id1=>$sub1) { ?>
		<tr>
			<td><?php echo $id1; ?></td>
			<td><?php echo var_export($sub1,true); ?></td>
		</tr>
		<?php

}
?>
</table>



</body>
</html>
