package cn.shaviation.autotest.annotation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

import cn.shaviation.autotest.util.Logs;

public abstract class TestMethodFinder {

	public static void search(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		SearchPattern pattern = SearchPattern.createPattern(
				TestMethod.class.getName(), IJavaSearchConstants.TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE);
		IJavaSearchScope scope = SearchEngine
				.createJavaSearchScope(new IJavaElement[] { javaProject });
		final SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match)
					throws CoreException {
				Logs.i(match.toString());
				if (match instanceof TypeReferenceMatch) {
					IJavaElement element = ((TypeReferenceMatch) match).getLocalElement();
					if (element instanceof IAnnotation && element.getParent() instanceof IMethod) {
						IAnnotation annotation = (IAnnotation) element;
						for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
							Logs.i(pair.getMemberName() + "=" + pair.getValue());
						}
						IMethod method = (IMethod) element.getParent();
						Logs.i(method.getDeclaringType().getFullyQualifiedName() + "#" + method.getElementName());
					}
				}
			}
		};
		new SearchEngine().search(pattern,
				new SearchParticipant[] { SearchEngine
						.getDefaultSearchParticipant() }, scope, requestor,
				null);
	}
}
