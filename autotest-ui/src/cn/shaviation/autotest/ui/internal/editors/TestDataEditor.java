package cn.shaviation.autotest.ui.internal.editors;

import java.util.Date;

import cn.shaviation.autotest.core.model.TestDataDef;

public class TestDataEditor extends DocumentFormEditor<TestDataDef> {

	@Override
	protected DocumentSourcePage<TestDataDef> createSourcePage() {
		return new TestDataSourcePage(this);
	}

	@Override
	protected DocumentFormPage<TestDataDef> createEditorPage() {
		return new TestDataFormPage(this);
	}

	@Override
	protected void beforeSave(TestDataDef model) {
		super.beforeSave(model);
		model.setLastUpdateTime(new Date());
	}
}
