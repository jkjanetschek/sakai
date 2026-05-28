
// Cache the show items and hide items links
var showItems = $('#show-items');
var hideItems = $('#hide-items');

$(function() {

$('a.itemListToggle').on('click', function () {

    $(this).parents('li').find('.itemList').toggle();


    if(typeof window.frameElement !== 'undefined') {
        setMainFrameHeight(window.frameElement.id);
    }

    return false;
});

$('#show-items').on('click', function (e) {

    showItems.hide();
    hideItems.show();
    $('.itemList').show();
    if(typeof window.frameElement !== 'undefined') {
        setMainFrameHeight(window.frameElement.id);
    }
    return false;
});

$('#hide-items').on('click', function (e) {

    showItems.show();
    hideItems.hide();
    $('.itemList').hide();
    if(typeof window.frameElement !== 'undefined') {
        setMainFrameHeight(window.frameElement.id);
    }
    return false;
});

$("#chooseall").change(function(){
	$(".deletebox").prop('checked', $("#chooseall").prop('checked'));
});

// Subpage warning for "Add items from another page" (reorder mode only)
    var isReorderMode = $('#reorder-mode-flag').length > 0;
    if (isReorderMode) {
        $('input[type="radio"]').on('change', function () {
            var hasSubpages = $(this).attr('data-has-subpages') === 'true';
            if (hasSubpages) {
                $('#subpage-warning').show();
            } else {
                $('#subpage-warning').hide();
            }
            if (typeof window.frameElement !== 'undefined') {
                setMainFrameHeight(window.frameElement.id);
            }
        });
    }

});

    
