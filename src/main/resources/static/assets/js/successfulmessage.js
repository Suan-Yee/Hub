function showToast(action,color) {
    Toastify({
        text: action+ " Successful",
        duration: 3000, // Duration in milliseconds (e.g., 3000 = 3 seconds)
        position: 'center',
        gravity: "center", // Toast position (e.g., "bottom", "top", "center")
        style: {
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            textAlign: 'center',
            background: color, // Set the background color using the style property
        },
        close: true, // Show close button or not
        stopOnFocus: true, // Stop countdown when the toast is focused
        /*  callback: function() {
              if (typeof callback === 'function') {
                  callback();
              }
          }*/
    }).showToast();
}