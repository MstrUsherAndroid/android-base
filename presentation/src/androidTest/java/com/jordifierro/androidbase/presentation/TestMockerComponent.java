package com.jordifierro.androidbase.presentation;

import android.app.Application;

import com.jordifierro.androidbase.presentation.dependency.ApplicationScope;
import com.jordifierro.androidbase.presentation.dependency.component.DemoActivityInjector;
import com.jordifierro.androidbase.presentation.dependency.component.DemoFragmentInjector;
import com.jordifierro.androidbase.presentation.dependency.module.ApplicationModule;
import com.jordifierro.androidbase.presentation.dependency.module.DataModule;
import com.jordifierro.androidbase.presentation.dependency.module.DemoApplicationModule;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**

 */
@ApplicationScope
@Component(modules = {
        DemoApplicationModule.class,
        ApplicationModule.class,
        AndroidInjectionModule.class,
        DataModule.class,
        DemoActivityInjector.class,
        DemoFragmentInjector.class,
        TestMockerModule.class
})
public interface TestMockerComponent {

    void inject(BaseApplication app);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        TestMockerComponent build();
    }
}
