function validateAndCreateWidget() {
  var widget = computeWidget();
  return widget;
}

function cancelUpdateWidget() {
  window.location.href = "/manager/widgets";
}

function validateAndUpdateWidget() {
  var widget = computeUpdateWidget();
  return widget;
}

function computeWidget() {
  var widget = {};

  var inputName = $("#name");
  var inputType = $("#type");
  var inputAsynchronous = $("#asynchronous");
  var inputLocaleCode = $("#localeCode");

  widget.name = inputName.val();
  widget.type = inputType.val();
  widget.localeCode = inputLocaleCode.val();
  widget.asynchronous = inputAsynchronous.is(':checked');

  return widget;

}

function computeUpdateWidget() {
  var widget = {};

  var inputName = $("#name");
  var inputType = $("#type");
  var inputEntityId = $("#entityId");
  var inputId = $("#id");
  var inputCreationDate = $("#creationDate");
  var inputModificationDate = $("#modificationDate");
  var inputLocaleCode = $("#localeCode");
  var inputCreationUser = $("#creationUser");
  var inputModificationUser = $("#modificationUser");
  var inputAsynchronous = $("#asynchronous");

  widget.name = inputName.val();
  if (inputAsynchronous.is(":checkbox")) {
    widget.asynchronous = inputAsynchronous.is(':checked');
  } else {
    widget.asynchronous = inputAsynchronous.val();
  }

  widget.type = inputType.val();
  widget.entityId = inputEntityId.val();
  widget.personalization = computePersonalization();
  widget.id = inputId.val();
  widget.creationDate = inputCreationDate.val();
  widget.modificationDate = inputModificationDate.val();
  widget.localeCode = inputLocaleCode.val();
  widget.creationUser = inputCreationUser.val();
  widget.modificationUser = inputModificationUser.val();
  return widget;
}

function computePersonalization() {
  if (codeMirrorPersonalization) {
    return codeMirrorPersonalization.getValue();
  }
  return $("#personalization").val();
}

function postCreateWidgetForm() {
  var url = "/manager/widgets";
  create($("#widgetCreateForm"), $(".loader"), $(".card-loader"), url, url,
      computeWidget()).done(function (data) {
    handleSuccessPostResult(data, $(".card-loader"), $(".loader"),
        $("#widgetCreateForm"), url);
  }).fail(function (error) {
    handleErrorPostResult($(".loader"), $(".card-loader"),
        $("#widgetCreateForm"));
  });
}

function postUpdateWidgetForm() {
  var widgetToUpdate = computeUpdateWidget();
  var url = "/manager/widgets/" + widgetToUpdate.id;
  var urlFallback = "/manager/widgets/" + widgetToUpdate.id;
  update($("#widgetUpdateForm"), $(".loader"), $(".card-loader"), url,
      urlFallback, widgetToUpdate, true).done(function (data) {
    handleSuccessPutResult(data, $(".card-loader"), $(".loader"),
        $("#widgetUpdateForm"), url, true);
    currentTab = "";
    goToWidgetMainTab();
  }).fail(function (error) {
    handleErrorPutResult($(".loader"), $(".card-loader"),
        $("#widgetUpdateForm"));
  });
}

function postUpdateWidgetPersonalization() {
  var widgetToUpdate = computeUpdateWidget();
  var url = "/manager/widgets/" + widgetToUpdate.id;
  var urlFallback = "/manager/widgets/" + widgetToUpdate.id;
  update($("#widgetUpdateForm"), $(".loader"), $(".card-loader"), url,
      urlFallback, widgetToUpdate, true).done(function (data) {
    handleSuccessPutResult(data, $(".card-loader"), $(".loader"),
        $("#widgetUpdateForm"), url, true);
    currentTab = "";
    goToPersonalizationTab();
  }).fail(function (error) {
    handleErrorPutResult($(".loader"), $(".card-loader"),
        $("#widgetUpdateForm"));
  });
}

