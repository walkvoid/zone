$(window).on("load", function () {
	$(".preloader").fadeOut("slow");
});

$(function () {
	$('[data-bs-toggle="popover"]').popover();
	$('[data-bs-toggle="tooltip"]').tooltip();
})

$(function () {
	"use strict";
	new PerfectScrollbar(".top-message-list"),
		new PerfectScrollbar(".top-notifications-list"),
		new PerfectScrollbar("#menu"),

		$(".mobile-search-icon").on("click", function () {
			$(".search-bar").addClass("full-search-bar")
		}),
		$(".search-close").on("click", function () {
			$(".search-bar").removeClass("full-search-bar")
		}),
		$(".mobile-toggle-menu").on("click", function () {
			$(".main-wrapper").addClass("toggled")
		}),

		$(".sidebar-toggle-icon").click(function () {
			$(".main-wrapper").hasClass("toggled") ? ($(".main-wrapper").removeClass("toggled"), $(".sidebar").unbind("hover")) : ($(".main-wrapper").addClass("toggled"), $(".sidebar").hover(function () {
				$(".main-wrapper").addClass("sidebar-hovered")
			}, function () {
				$(".main-wrapper").removeClass("sidebar-hovered")
			}))
		}),
		$(function () {
			$("#menu").metisMenu();
		}),
		$(function () {
			for (var e = window.location, o = $(".metismenu li a").filter(function () {
				return this.href == e
			}).addClass("").parent().addClass("mm-active"); o.is("li");) o = o.parent("").addClass("mm-show").parent("").addClass("mm-active")
		}),
	// Bootstrap 5 Validation
	$(".needs-validation").submit(function (e) {
		var form = $(this);
		if (form[0].checkValidity() === false) {
			e.preventDefault();
			e.stopPropagation();
		}
		form.addClass("was-validated");
	});

    //Dark/Light Mode
    $('#nightmode-btn').on('click', function (e) {
        let thememode = localStorage.getItem("thememode");
        if (thememode == 'dark') {
            localStorage.setItem("thememode", "light");
        } else {
            localStorage.setItem("thememode", "dark");
        }
        load_theme_setting();
    });
    //Show Theme Setting Panel
    $('.theme-customizer .handle').on('click', function (e) {
        if ($('.theme-customizer').hasClass('open')) {
            $('.theme-customizer').removeClass('open');
        }
        else {
            $('.theme-customizer').addClass('open');
        }
    });
    $('.theme-customizer #closeThemeSetting').on('click', function (e) {
        $('.theme-customizer').removeClass('open');
    });
    //When click on theme color
    $('.theme-customizer .indigator').on('click', function (e) {
        let theme = $(this).data('id');
        localStorage.setItem("themecolor", theme);
        load_theme_setting();
    });
    //Switch theme rtl/ltr
    $('.theme-customizer #rtlCheckbox').on('click', function (e) {
        let thememode = localStorage.getItem("themedirection");
        if (thememode == 'rtl') {
            localStorage.setItem("themedirection", "ltr");
        } else {
            localStorage.setItem("themedirection", "rtl");
        }
        load_theme_setting();
    });
    //Switch Light/Dark
    $('.theme-customizer #darkModeCheckbox').on('click', function (e) {
        var thememode = localStorage.getItem("thememode");
        if (thememode == 'dark') {
            localStorage.setItem("thememode", "light");
        } else {
            localStorage.setItem("thememode", "dark");
        }
        load_theme_setting();
    });

	//sidebar color
	$('.theme-customizer #themesidebarlight').on('click', function (e) {
        localStorage.setItem("themesidebar", "light");
        load_theme_setting();
    });
	$('.theme-customizer #themesidebardark').on('click', function (e) {
        localStorage.setItem("themesidebar", "dark");
        load_theme_setting();
    });
	$('.theme-customizer #themesidebarprimary').on('click', function (e) {
        localStorage.setItem("themesidebar", "sidebar-primary");
        load_theme_setting();
    });

    //Reset Button Click
    $('.theme-customizer #resetSetting').on('click', function (e) {
        localStorage.setItem("thememode", "light");
        localStorage.setItem("themecolor", "");
        localStorage.setItem("themedirection", "ltr");
        localStorage.setItem("themesidebar", "light");
        window.location.reload();
    });

    var load_theme_setting = function () {
        let thememode = localStorage.getItem("thememode");
        let themecolor = localStorage.getItem("themecolor");
        let themedirection = localStorage.getItem("themedirection");
		let themesidebar = localStorage.getItem("themesidebar");

		if (themesidebar != null) {
            $('.sidebar').attr('class', 'sidebar');
			$('.sidebar').addClass(themesidebar);
        }

        if (themecolor != null) {
            $('html').attr('class', themecolor);
        }
        else{
            $('html').attr('class', '');
        }
        if (thememode == 'dark') {
            $('html').addClass('dark');
            $('#darkModeCheckbox').attr('checked', 'checked');
        }
        else {
            $('html').addClass('light');
            $('#darkModeCheckbox').attr('checked', false);
        }
        if (themedirection == 'rtl') {
            $('[href = "assets/lib/bootstrap/bootstrap.min.css"]').attr('href', "assets/lib/bootstrap/bootstrap-rtl.min.css");
            $('html').attr('dir', 'rtl');
            $('#rtlCheckbox').attr('checked', 'checked');
        }
        else {
            $('[href = "assets/lib/bootstrap/bootstrap-rtl.min.css"]').attr('href', "assets/lib/bootstrap/bootstrap.min.css");
            $('html').attr('dir', 'ltr');
            $('#rtlCheckbox').attr('checked', false);
        }
    };
    load_theme_setting();

	// full screen call
	$(document).on("click", "#fullscreen-btn", function (e) {
		if (
			!document.fullscreenElement && // alternative standard method
			!document.mozFullScreenElement &&
			!document.webkitFullscreenElement &&
			!document.msFullscreenElement
		) {
			// current working methods
			if (document.documentElement.requestFullscreen) {
				document.documentElement.requestFullscreen();
			} else if (document.documentElement.msRequestFullscreen) {
				document.documentElement.msRequestFullscreen();
			} else if (document.documentElement.mozRequestFullScreen) {
				document.documentElement.mozRequestFullScreen();
			} else if (document.documentElement.webkitRequestFullscreen) {
				document.documentElement.webkitRequestFullscreen(
					Element.ALLOW_KEYBOARD_INPUT
				);
			}
		} else {
			if (document.exitFullscreen) {
				document.exitFullscreen();
			} else if (document.msExitFullscreen) {
				document.msExitFullscreen();
			} else if (document.mozCancelFullScreen) {
				document.mozCancelFullScreen();
			} else if (document.webkitExitFullscreen) {
				document.webkitExitFullscreen();
			}
		}
	});
});