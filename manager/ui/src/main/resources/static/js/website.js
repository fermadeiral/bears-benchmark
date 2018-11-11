function cancelUpdateWebsite() {
  window.location.href = "/manager/websites";
}

function cancelCreateWebsite() {
  window.location.href = "/manager/websites";
}

function computeWebsiteToUpdate() {
  var website = {};

  var inputName = $("#name");
  var inputExtension = $("#extension");
  var inputSecure = $("#secure");
  var inputSystemJquery = $("#systemJquery");
  var inputSystemBootstrap = $("#systemBootstrap");
  var inputSystemFontAwesome = $("#systemFontAwesome");
  var inputId = $("#id");
  var inputCreationDate = $("#creationDate");
  var inputModificationDate = $("#modificationDate");
  var inputCreationUser = $("#creationUser");
  var inputModificationUser = $("#modificationUser");

  website.name = inputName.val();
  website.description = computeWebsiteDescription();
  website.extension = inputExtension.val();
  website.secure = inputSecure.is(':checked');
  website.systemJquery = inputSystemJquery.is(':checked');
  website.systemBootstrap = inputSystemBootstrap.is(':checked');
  website.systemFontAwesome = inputSystemFontAwesome.is(':checked');

  website.id = inputId.val();
  website.creationDate = formatDate(inputCreationDate.val());
  website.modificationDate = formatDate(inputModificationDate.val());
  website.creationUser = inputCreationUser.val();
  website.modificationUser = inputModificationUser.val();

  return website;

}

function computeWebsiteToCreate() {
  var website = {};

  var inputName = $("#name");
  var inputExtension = $("#extension");
  var inputSecure = $("#secure");
  var inputSystemJquery = $("#systemJquery");
  var inputSystemBootstrap = $("#systemBootstrap");
  var inputSystemFontAwesome = $("#systemFontAwesome");

  website.name = inputName.val();
  website.extension = inputExtension.val();
  website.description = computeWebsiteDescription();
  website.secure = inputSecure.is(':checked');
  website.systemJquery = inputSystemJquery.is(':checked');
  website.systemBootstrap = inputSystemBootstrap.is(':checked');
  website.systemFontAwesome = inputSystemFontAwesome.is(':checked');

  return website;

}

function computeWebsiteDescription() {
  var description = "";
  description = CKEDITOR.instances.description.getData();
  return description;
}

function postUpdateWebsiteForm() {
  var websiteToUpdate = computeWebsiteToUpdate();
  var url = "/manager/websites/" + websiteToUpdate.id;
  var urlFallback = "/manager/websites/" + websiteToUpdate.id;
  update($("#websiteUpdateForm"), $(".loader"), $(".card-loader"), url,
      urlFallback,
      websiteToUpdate).done(function (data) {
    handleSuccessPutResult(data, $(".card-loader"), $(".loader"),
        $("#websiteUpdateForm"), url, true);
    currentTab = "";
    goToWebsiteMainTab();
  }).fail(function (error) {
    handleErrorPutResult($(".loader"), $(".card-loader"),
        $("#websiteUpdateForm"));
  });
}

function postCreateWebsiteForm() {
  var websiteToCreate = computeWebsiteToCreate();
  var url = "/manager/websites/";
  var urlFallback = "/manager/websites/";
  create($("#websiteCreateForm"), $(".loader"), $(".card-loader"), url,
      urlFallback, websiteToCreate).done(function (data) {
    handleSuccessPostResult(data, $(".card-loader"), $(".loader"),
        $("#websiteCreateForm"), url);
  }).fail(function (error) {
    handleErrorPostResult($(".loader"), $(".card-loader"),
        $("#websiteCreateForm"));
  });
}
