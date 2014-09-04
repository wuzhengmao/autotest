package cn.shaviation.autotest.ui.internal.editors;

import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestScriptHelper;

public class TestScriptSourcePage extends DocumentSourcePage<TestScript> {

	public TestScriptSourcePage(TestScriptEditor editor) {
		super(editor);
	}

	@Override
	protected String convertModelToSource(TestScript model) throws Exception {
		return TestScriptHelper.serialize(model);
	}
}
