# Google Search Console API example

Get Google Search Console crawl data using the API

## Prerequisites

Required steps in order to access the API:
1. create a project (https://console.developers.google.com/apis/dashboard) and enable the Google Console API for it.
2. create a Service account key (in order to use OAuth without user interaction)
3. allow access to the site data
   - go to https://www.google.com/webmasters/tools/home
   - (for each site) select manage property -> Add or remove users
   - add new user (use the service account key email) with permission Restricted (gives read only access) 


## Usage

```bash
java -jar google-console-api.jar <path-to-credentials> <site-url> <category> <platform>
```

The values for *category* & *platform* can be taken from https://developers.google.com/webmaster-tools/search-console-api-original/v3/urlcrawlerrorssamples/list#Parameters

### Example

```bash
java -jar google-console-api.jar credentials.json https://www.mydomain.com notFound web
```

## Resources

- Authenticating with service accounts https://developers.google.com/identity/protocols/OAuth2ServiceAccount 
- Google API client libraries https://developers.google.com/api-client-library/
- Search Console https://www.google.com/webmasters/tools/home?hl=en
- API Dashboard https://console.developers.google.com/apis/dashboard
- API Explorer https://developers.google.com/apis-explorer/#p/webmasters/v3/
- API Documentation https://developers.google.com/webmaster-tools/search-console-api-original/v3/
- API Samples https://developers.google.com/webmaster-tools/search-console-api-original/v3/samples