package cn.shaviation.autotest.ui.internal.editors;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataHelper;

public class TestDataSourcePage extends DocumentSourcePage<TestDataDef> {

	public TestDataSourcePage(TestDataEditor editor) {
		super(editor);
	}

	@Override
	protected String convertModelToSource(TestDataDef model) throws Exception {
		return TestDataHelper.serialize(model);
	}
}
