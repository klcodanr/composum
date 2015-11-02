<%@page session="false" pageEncoding="utf-8"%>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2"%>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0"%>
<sling:defineObjects/>
<cpn:component id="browser" type="com.composum.sling.core.browser.Browser" scope="request">
  <div id="text-edit-dialog" class="dialog modal fade" role="dialog" aria-labelledby="Edit Text"
       aria-hidden="true">
    <div class="text-editor detail-panel">
      <div class="modal-dialog">
        <div class="modal-content widget code-editor-widget">

          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                aria-hidden="true">&times;</span></button>
            <h4 class="modal-title"></h4>
          </div>

          <div class="modal-body">
            <div class="code-editor">
            </div>
          </div>

          <div class="modal-footer buttons">
            <div class="toolbar">
              <div class="search input-group input-group-sm text-group">
                <input type="text" class="find-text form-control" placeholder="search in text">
                <span class="find-prev fa fa-chevron-left input-group-addon" title="find previous"></span>
                <span class="find-next fa fa-chevron-right input-group-addon" title="find next"></span>
              </div>
              <input type="checkbox" class="match-case form-control" title="regex"><span class="checkbox-label">match case</span>
              <input type="checkbox" class="find-regex form-control" title="regex"><span class="checkbox-label">regex</span>
              <div class="replace input-group input-group-sm text-group">
                <input type="text" class="replace-text form-control" placeholder="replace with...">
                <span class="replace fa fa-play input-group-addon" title="replace this"></span>
                <span class="replace-all fa fa-fast-forward input-group-addon" title="replace all"></span>
              </div>
              <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="undo fa fa-undo btn btn-default" title="Undo last change"><span class="label">Undo</span></button>
                <button type="button" class="redo fa fa-repeat btn btn-default" title="Redo last undo"><span class="label">Redo</span></button>
              </div>
            </div>
            <button type="button" class="btn btn-default cancel" data-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-primary save">Save</button>
          </div>

        </div>
      </div>
    </div>
  </div>
</cpn:component>