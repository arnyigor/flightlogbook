package com.arny.flightlogbook;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InstrumentsTests {
	private static final Context context = InstrumentationRegistry.getTargetContext();
	private static final Object syncObject = new Object();

	private static class ImmediateSchedulersRule implements TestRule {
		@Override
		public Statement apply(final Statement base, Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
					RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
					RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
					try {
						base.evaluate();
					} finally {
						RxJavaPlugins.reset();
					}
				}
			};
		}
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Rule
	public final ImmediateSchedulersRule schedulers = new ImmediateSchedulersRule();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
}
