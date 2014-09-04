package cn.shaviation.autotest.ui.internal.editors;

import java.util.Date;

import cn.shaviation.autotest.model.TestScript;

public class TestScriptEditor extends DocumentFormEditor<TestScript> {

	@Override
	protected DocumentSourcePage<TestScript> createSourcePage() {
		return new TestScriptSourcePage(this);
	}

	@Override
	protected DocumentFormPage<TestScript> createEditorPage() {
		return new TestScriptFormPage(this);
	}

	@Override
	protected void beforeSave(TestScript model) {
		super.beforeSave(model);
		model.setLastUpdateTime(new Date());
	}
}
