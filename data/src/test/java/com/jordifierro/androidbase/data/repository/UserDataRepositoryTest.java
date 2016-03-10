package com.jordifierro.androidbase.data.repository;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.jordifierro.androidbase.data.net.RestApi;
import com.jordifierro.androidbase.data.net.error.RestApiErrorException;
import com.jordifierro.androidbase.data.utils.TestUtils;
import com.jordifierro.androidbase.domain.entity.UserEntity;
import com.jordifierro.androidbase.domain.executor.DefaultThreadExecutor;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class UserDataRepositoryTest{

    private UserDataRepository userDataRepository;
    private TestSubscriber testSubscriber;
    private MockWebServer mockWebServer;
    private UserEntity mockUser;

    @Before
    public void setUp() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.userDataRepository = new UserDataRepository(
                new Retrofit.Builder()
                        .baseUrl(mockWebServer.url("/"))
                        .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build()
                        .create(RestApi.class)
        );
        this.testSubscriber = new TestSubscriber();
        MockitoAnnotations.initMocks(this);
        this.mockUser = new UserEntity("mock@mail.com");
        this.mockUser.setPassword("1234");
        this.mockUser.setPasswordConfirmation("1234");
    }

    @After
    public void tearDown() throws Exception {
        this.mockWebServer.shutdown();
    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(201).setBody(
                FileUtils.readFileToString(
                        TestUtils.getFileFromPath(this, "res/user_create_ok.json"))));

        this.userDataRepository.createUser(this.mockUser)
                                    .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                                    .observeOn(Schedulers.io())
                                    .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        UserEntity responseUser = (UserEntity) this.testSubscriber.getOnNextEvents().get(0);
        assertTrue(responseUser.getEmail().length() > 0);
        assertTrue(responseUser.getAuthToken().length() > 0);
    }

    @Test
    public void testCreateUserError() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(422).setBody(
                FileUtils.readFileToString(
                        TestUtils.getFileFromPath(this, "res/user_create_error.json"))));

        this.userDataRepository.createUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        this.testSubscriber.assertValueCount(0);
        RestApiErrorException error = (RestApiErrorException)
                this.testSubscriber.getOnErrorEvents().get(0);
        assertEquals(422, error.getStatusCode());
        assertEquals("Email has already been taken", error.getMessage());
    }

    @Test
    public void testDeleteUserSuccess() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        this.userDataRepository.deleteUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        this.testSubscriber.assertValueCount(1);
    }

    @Test
    public void testDeleteUserError() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        this.userDataRepository.deleteUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        this.testSubscriber.assertValueCount(0);
        RestApiErrorException error = (RestApiErrorException)
                this.testSubscriber.getOnErrorEvents().get(0);
        assertEquals(401, error.getStatusCode());
        assertTrue(error.getMessage().length() > 0);
    }

    @Test
    public void testLoginUserSuccess() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                FileUtils.readFileToString(
                        TestUtils.getFileFromPath(this, "res/session_login_ok.json"))));


        this.userDataRepository.loginUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        UserEntity responseUser = (UserEntity) this.testSubscriber.getOnNextEvents().get(0);
        assertTrue(responseUser.getEmail().length() > 0);
        assertTrue(responseUser.getAuthToken().length() > 0);
    }

    @Test
    public void testLoginUserError() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(422).setBody(
                FileUtils.readFileToString(
                        TestUtils.getFileFromPath(this, "res/session_login_error.json"))));


        this.userDataRepository.loginUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        this.testSubscriber.assertValueCount(0);
        RestApiErrorException error = (RestApiErrorException)
                this.testSubscriber.getOnErrorEvents().get(0);
        assertEquals(422, error.getStatusCode());
        assertEquals("Invalid email or password.", error.getMessage());
    }

    @Test
    public void testLogoutUserSuccess() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        this.userDataRepository.logoutUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        this.testSubscriber.assertValueCount(1);
    }

    @Test
    public void testLogoutUserError() throws Exception {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        this.userDataRepository.logoutUser(this.mockUser)
                .subscribeOn(Schedulers.from(new DefaultThreadExecutor()))
                .observeOn(Schedulers.io())
                .subscribe(this.testSubscriber);
        this.testSubscriber.awaitTerminalEvent();

        this.testSubscriber.assertValueCount(0);
        RestApiErrorException error = (RestApiErrorException)
                this.testSubscriber.getOnErrorEvents().get(0);
        assertEquals(401, error.getStatusCode());
        assertTrue(error.getMessage().length() > 0);
    }

}