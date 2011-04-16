$(function() {
  $.getJSON('/token', function(data) {
    console.log('get token');
    var token = data.token;
    var channel = new goog.appengine.Channel(token);
    var socket = channel.open({
      onopen: function() {
        console.log('open');
        $.getJSON('/opened', function(data) {
          console.log('opend:', data.status);
        });
      },
      onmessage: function(message) {
        console.log(message);
        var data = $.parseJSON(message.data);
        $(
          '<p>' +
            '<span>' + data.user + '</span>' +
             '&nbsp;&raquo;&nbsp;' +
             data.message +
          '</p>'
        ).prependTo('#log');
        $("form #text").val('');
      },
      onerror: function(error) {
        console.log(error);
        $(
          '<p class="error">' +
            'ERROR: [' + error.code + ']' + error.description +
          '</p>'
        ).prependTo('#log');
      },
      onclose: function() {
        console.log('close')
      }
    });

    $("form").submit(function() {
      var message = $("form #text").val();
      if(message) {
        $.post('/send-message', {message: message}, function(data) {
          console.log(data);
        })
      }
      return false;
    });
  });
});
