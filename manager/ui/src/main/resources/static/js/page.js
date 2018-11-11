function cancelUpdatePage() {
  window.location.href = "/manager/pages";
}

function computePage() {
  var page = {};

  var inputName = $("#name");
  var inputMenuTitle = $("#menuTitle");
  var inputHref = $("#href");
  var inputLocaleCode = $("#localeCode");
  var inputIndexed = $("#indexed");

  page.indexed = inputIndexed.is(':checked');
  page.name = inputName.val();
  page.href = inputHref.val();
  page.menuTitle = inputMenuTitle.val();
  page.body = computePageBody();
  page.header = computePageHeader();
  page.footer = computePageFooter();
  page.meta = computePageMeta();
  page.localeCode = inputLocaleCode.val();

  return page;

}

function computeUpdatePage() {
  var page = {};

  var inputName = $("#name");
  var inputHref = $("#href");
  var inputMenuTitle = $("#menuTitle");
  var inputId = $("#id");
  var inputCreationDate = $("#creationDate");
  var inputModificationDate = $("#modificationDate");
  var inputCreationUser = $("#creationUser");
  var inputModificationUser = $("#modificationUser");
  var inputLocaleCode = $("#localeCode");
  var inputIndexed = $("#indexed");

  if (inputIndexed.is(":checkbox")) {
    page.indexed = inputIndexed.is(':checked');
  } else {
    page.indexed = inputIndexed.val();
  }

  page.name = inputName.val();
  page.href = inputHref.val();
  page.menuTitle = inputMenuTitle.val();
  page.body = computePageBody();
  page.header = computePageHeader();
  page.footer = computePageFooter();
  page.meta = computePageMeta();
  page.id = inputId.val();
  page.amp = computePageAMP();
  page.creationDate = inputCreationDate.val();
  page.modificationDate = inputModificationDate.val();
  page.creationUser = inputCreationUser.val();
  page.modificationUser = inputModificationUser.val();
  page.localeCode = inputLocaleCode.val();
  return page;
}

function computePageBody() {
  if (codeMirrorBody) {
    return codeMirrorBody.getValue();
  }
  return $("#body").val();
}

function computePageHeader() {
  if (codeMirrorHeader) {
    return codeMirrorHeader.getValue();
  }
  return $("#header").val();
}

function computePageAMP() {
  if (codeMirrorAMP) {
    return codeMirrorAMP.getValue();
  }
  return $("#amp").val();
}

function computePageMeta() {
  if (codeMirrorMeta) {
    return codeMirrorMeta.getValue();
  }
  return $("#meta").val();
}

function computePageFooter() {
  if (codeMirrorFooter) {
    return codeMirrorFooter.getValue();
  }
  return $("#footer").val();
}

function postCreatePageForm() {
  var url = "/manager/pages";
  create($("#pageCreateForm"), $(".loader"), $(".card-loader"), url, url,
      computePage()).done(function (data) {
    handleSuccessPostResult(data, $(".card-loader"), $(".loader"),
        $("#pageCreateForm"), url)
  }).fail(function (error) {
    handleErrorPostResult($(".loader"), $(".card-loader"),
        $("#pageCreateForm"));
  });
}

function postUpdatePageForm(successFunction) {
  var pageToUpdate = computeUpdatePage();
  var url = "/manager/pages/" + pageToUpdate.id;
  var urlFallback = "/manager/pages/" + pageToUpdate.id;
  update($("#pageUpdateForm"), $(".loader"), $(".card-loader"), url,
      urlFallback, pageToUpdate, true).done(function (data) {
    handleSuccessPutResult(data, $(".card-loader"), $(".loader"),
        $("#pageUpdateForm"), url, true);
    currentTab = "";
    if (successFunction) {
      successFunction();
    }
  }).fail(function (error) {
    handleErrorPutResult($(".loader"), $(".card-loader"),
        $("#pageUpdateForm"));
  });
}

$(document).ready(function () {
  $("#formInputWithNews").click(function () {
    $("#withNews").click();
  });
});
