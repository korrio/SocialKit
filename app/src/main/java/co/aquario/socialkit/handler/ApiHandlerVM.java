package co.aquario.socialkit.handler;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import co.aquario.socialkit.event.FailedNetworkEvent;
import co.aquario.socialkit.event.FbAuthEvent;
import co.aquario.socialkit.event.FriendListDataResponse;
import co.aquario.socialkit.event.GetStoryEvent;
import co.aquario.socialkit.event.GetStorySuccessEvent;
import co.aquario.socialkit.event.LoadFriendListEvent;
import co.aquario.socialkit.event.LoadFriendListSuccessEvent;
import co.aquario.socialkit.event.LoadTimelineEvent;
import co.aquario.socialkit.event.LoadTimelineSuccessEvent;
import co.aquario.socialkit.event.LoginEvent;
import co.aquario.socialkit.event.LoginFailedAuthEvent;
import co.aquario.socialkit.event.LoginSuccessEvent;
import co.aquario.socialkit.event.LogoutEvent;
import co.aquario.socialkit.event.RegisterEvent;
import co.aquario.socialkit.event.RegisterFailedEvent;
import co.aquario.socialkit.event.RegisterSuccessEvent;
import co.aquario.socialkit.event.RequestOtpEvent;
import co.aquario.socialkit.event.StoryDataResponse;
import co.aquario.socialkit.event.TimelineDataResponse;
import co.aquario.socialkit.model.LoginData;
import co.aquario.socialkit.model.RegisterData;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by matthewlogan on 9/3/14.
 */
public class ApiHandlerVM {

    private Context context;
    private ApiServiceVM api;
    private ApiBus apiBus;

    public ApiHandlerVM(Context context, ApiServiceVM api,
                        ApiBus apiBus) {

        this.context = context;
        this.api = api;
        this.apiBus = apiBus;
    }

    public void registerForEvents() {
        apiBus.register(this);
    }

    @Subscribe public void onLoginEvent(LoginEvent event) {
        Log.e("HEY2!","Login: " +event.getUsername() + " : " + event.getPassword());

        Map<String, String> options = new HashMap<String, String>();
        options.put("username", event.getUsername());
        options.put("password", event.getPassword());

        api.login(options, new Callback<LoginData>() {
            @Override
            public void success(LoginData loginData, Response response) {
                //Log.e("loginData",loginData.apiToken);
                Log.e("response",response.getBody().toString());

                if(loginData.status.equals("1"))
                    apiBus.post(new LoginSuccessEvent(loginData));
                else
                    apiBus.post(new LoginFailedAuthEvent());

                Log.e("POSTBACK","post response back to LoginFragment");
            }

            @Override
            public void failure(RetrofitError error) {
                //Log.e("response",error.getBody().toString());
                Log.e("failedNetwork","failedNetworkEvent");
                apiBus.post(new FailedNetworkEvent());
            }
        });
    }

    @Subscribe public void onFbLoginEvent(FbAuthEvent event) {

        Map<String, String> options = new HashMap<String, String>();
        options.put("access_token", event.getFbToken());

        api.fbLogin(options, new Callback<LoginData>() {
            @Override
            public void success(LoginData loginData, Response response) {
                //Log.e("loginData",loginData.apiToken);
                Log.e("response",response.getBody().toString());

                if(loginData.status.equals("1"))
                    apiBus.post(new LoginSuccessEvent(loginData));
                else
                    apiBus.post(new LoginFailedAuthEvent());

                Log.e("POSTBACK","post response back to LoginFragment");
            }

            @Override
            public void failure(RetrofitError error) {
                //Log.e("response",error.getBody().toString());
                apiBus.post(new FailedNetworkEvent());
            }
        });
    }

    @Subscribe public void onRegisterEvent(RegisterEvent event) {
        Map<String, String> options = new HashMap<String, String>();

        options.put("name", event.getName());
        options.put("username", event.getUsername());
        options.put("password", event.getPassword());
        options.put("email", event.getEmail());
        options.put("gender", event.getGender());

        api.register(options, new Callback<RegisterData>() {
            @Override
            public void success(RegisterData registerData, Response response) {
                //Log.e("loginData",loginData.apiToken);
                Log.e("response", response.getBody().mimeType());

                if (registerData.status.equals("1")) {
                    apiBus.post(new RegisterSuccessEvent(registerData));
                }
                else {
                    apiBus.post(new RegisterFailedEvent(registerData.message));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("response", error.getBody().toString());
                apiBus.post(new FailedNetworkEvent());
            }
        });
    }

    @Subscribe public void onRequestOtp(RequestOtpEvent event) {
        Map<String, String> options = new HashMap<String, String>();

        options.put("mobile", event.getMobile());
        options.put("message", event.getMessage());

        api.otp(options);
    }

    @Subscribe public void onGetStory(GetStoryEvent event) {

        api.getStory(Integer.parseInt(event.getPostId()), new Callback<StoryDataResponse>() {
            @Override
            public void success(StoryDataResponse storyDataResponse, Response response) {
                GetStorySuccessEvent event = new GetStorySuccessEvent(storyDataResponse.getPost());
                ApiBus.getInstance().post(event);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
            }

        });

    }

    @Subscribe public void onHomeTimelineRequestEvent(LoadTimelineEvent event) {
        Map<String, String> options = new HashMap<String, String>();

        options.put("type", event.getType());
        options.put("page", Integer.toString(event.getPage()));
        options.put("per_page",Integer.toString(event.getPerPage()));

        if(event.getIsHome()) {
            api.getHomeTimeline(event.getUserId(),options,new Callback<TimelineDataResponse>() {
                @Override
                public void success(TimelineDataResponse timelineDataResponse, Response response) {
                    Log.e("timelineDataResponse",timelineDataResponse.getStatus().toString());
                    if(timelineDataResponse.getStatus().equals("1")) {
                        Log.e("timelineDataResponse",response.getBody().toString());
                        ApiBus.getInstance().post(new LoadTimelineSuccessEvent(timelineDataResponse));

                    } else {
                        //MainApplication.get(this).getPrefManager().isLogin().put(false);
                        Log.e("LOGOUT!","LOG OUT LAEW");
                        ApiBus.getInstance().post(new LogoutEvent());
                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("error",error.toString());
                }
            });
        } else {
            api.getUserTimeline(event.getUserId(),options,new Callback<TimelineDataResponse>() {
                @Override
                public void success(TimelineDataResponse timelineDataResponse, Response response) {
                    Log.e("timelineDataResponse",timelineDataResponse.getStatus().toString());
                    if(timelineDataResponse.getStatus().equals("1")) {
                        Log.e("timelineDataResponse",response.getBody().toString());
                        ApiBus.getInstance().post(new LoadTimelineSuccessEvent(timelineDataResponse));

                    } else {
                        //MainApplication.get(this).getPrefManager().isLogin().put(false);
                        Log.e("LOGOUT!","LOG OUT LAEW");
                        ApiBus.getInstance().post(new LogoutEvent());
                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("error",error.toString());
                }
            });
        }


    }

    @Subscribe public void onLoadFriendListEvent(final LoadFriendListEvent event) {
        Map<String, String> options = new HashMap<String, String>();

        //options.put("type", event.getType());
        options.put("page", Integer.toString(event.getPage()));
        options.put("per_page",Integer.toString(event.getPerPage()));

        switch (event.getType()) {
            case "FOLLOWING":
                api.getFollowing(event.getUserId(), options, new Callback<FriendListDataResponse>() {
                    @Override
                    public void success(FriendListDataResponse friendListDataResponse, Response response) {
                        Log.e("friendListDataResponse", friendListDataResponse.status);
                        if (friendListDataResponse.status.equals("1")) {
                            //Log.e("timelineDataResponse", response.getBody().toString());
                            ApiBus.getInstance().post(new LoadFriendListSuccessEvent(friendListDataResponse,event.getType()));

                        } else {
                            //MainApplication.get(this).getPrefManager().isLogin().put(false);
                            Log.e("LOGOUT!", "LOG OUT LAEW");
                            ApiBus.getInstance().post(new LogoutEvent());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("error", error.toString());
                    }
                });
                break;
            case "FOLLOWER":
                api.getFollower(event.getUserId(), options, new Callback<FriendListDataResponse>() {
                    @Override
                    public void success(FriendListDataResponse friendListDataResponse, Response response) {
                        Log.e("friendListDataResponse", friendListDataResponse.status);
                        if (friendListDataResponse.status.equals("1")) {
                            //Log.e("timelineDataResponse", response.getBody().toString());
                            ApiBus.getInstance().post(new LoadFriendListSuccessEvent(friendListDataResponse,event.getType()));

                        } else {
                            //MainApplication.get(this).getPrefManager().isLogin().put(false);
                            Log.e("LOGOUT!", "LOG OUT LAEW");
                            ApiBus.getInstance().post(new LogoutEvent());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("error", error.toString());
                    }
                });
                break;
            case "FRIEND":
                api.getFriend(event.getUserId(), options, new Callback<FriendListDataResponse>() {
                    @Override
                    public void success(FriendListDataResponse friendListDataResponse, Response response) {
                        Log.e("friendListDataResponse", friendListDataResponse.status);
                        if (friendListDataResponse.status.equals("1")) {
                            //Log.e("timelineDataResponse", response.getBody().toString());
                            ApiBus.getInstance().post(new LoadFriendListSuccessEvent(friendListDataResponse,event.getType()));

                        } else {
                            //MainApplication.get(this).getPrefManager().isLogin().put(false);
                            Log.e("LOGOUT!", "LOG OUT LAEW");
                            ApiBus.getInstance().post(new LogoutEvent());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("error", error.toString());
                    }
                });
                break;
        }


    }
}