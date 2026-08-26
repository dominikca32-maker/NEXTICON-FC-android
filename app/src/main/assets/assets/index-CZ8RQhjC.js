const __parts = 6;
const __texts = await Promise.all(
  Array.from({length: __parts}, (_, i) =>
    fetch("/assets/index-CZ8RQhjC.part" + String(i).padStart(3,"0") + ".txt").then(r => {
      if (!r.ok) throw new Error("cz chunk " + i + " " + r.status);
      return r.text();
    })
  )
);
let __src = __texts.join("");
__src = __src.replace(/from(["'])\.\//g, "from$1/assets/");
__src = __src.replace(/import\((["'])\.\//g, "import($1/assets/");
const __url = URL.createObjectURL(new Blob([__src], {type: "text/javascript"}));
const __m = await import(__url);
export const { AuthApiError, AuthClient, AuthError, AuthImplicitGrantRedirectError, AuthInvalidCredentialsError, AuthInvalidJwtError, AuthInvalidTokenResponseError, AuthPKCECodeVerifierMissingError, AuthPKCEGrantCodeExchangeError, AuthRefreshDiscardedError, AuthRetryableFetchError, AuthSessionMissingError, AuthUnknownError, AuthWeakPasswordError, CustomAuthError, FunctionRegion, FunctionsError, FunctionsFetchError, FunctionsHttpError, FunctionsRelayError, GoTrueAdminApi, GoTrueClient, PostgrestError, REALTIME_LISTEN_TYPES, REALTIME_POSTGRES_CHANGES_LISTEN_EVENT, REALTIME_PRESENCE_LISTEN_EVENTS, REALTIME_SUBSCRIBE_STATES, RealtimeChannel, RealtimeClient, RealtimePostgresFilterBuilder, RealtimePresence, SIGN_OUT_SCOPES, StorageApiError, SupabaseClient, WebSocketFactory, createClient, isAuthApiError, isAuthError, isAuthImplicitGrantRedirectError, isAuthRefreshDiscardedError, isAuthRetryableFetchError, isAuthSessionMissingError } = __m;
