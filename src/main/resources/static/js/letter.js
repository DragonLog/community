$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{
			"toName":toName,
			"content":content
		},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				$("#hintBody").text("发送成功！")
			} else {
				$("#hintBody").text(data.msg)
			}
			$("#hintModal").modal("show");
			setTimeout(function() {
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);

	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}

function delete_msg(letterId) {
	$.ajax({
		url: CONTEXT_PATH + "/letter/remove/" + letterId,
		type: "put",
		success: function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				location.reload();
			} else {
				alert(data.msg);
			}
		}
	});
	$(this).parents(".media").remove();
}