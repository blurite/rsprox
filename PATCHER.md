# Patcher

RSProx comes with built-in patchers that perform necessary changes to make the
proxy connect to the Old School servers through the proxy. We currently support
two types of patching - Native and RuneLite.

## Native Patcher

The [native patcher](https://github.com/blurite/rsprox/blob/master/patch/patch-native/src/main/kotlin/net/rsprox/patch/native/NativePatchCriteria.kt)
supports a wide range of options to make it a more
general-purpose tool that can be used for now only RSProxy, but also private
servers themselves. Within RSProx itself, the native client option is disabled
when running from MacOS - this is due to it being too fragile.

### Supported Features
The native patcher supports the following features:

- Any version after `213.2` (~April 2023).
  - The repository where it downloads the historic executables can be found [here](https://www.runewiki.org/archive/oldschool.runescape.com/native/).
- Selection between Windows and MacOS.
  - Windows patching has been tested to work on every version currently available.
  - MacOS patching is a lot more fragile and will only work on more recent options.
  Additionally, due to the way the natives are built in MacOS, patching strings
  gets a lot trickier. The only way to make it work reliably is to patch strings
  by padding the end of them with spaces, rather than terminating early.
- Changing the `javconfig` URL.
- Changing the `worldlist` URL.
- Changing site URLs universally across the binary (e.g. `runescape.com` -> `rsprox.net`).
- Changing the name throughout the natives (e.g. `Old School RuneScape` -> `RSProx`).
- Changing the RSA modulus.
- Whitelisting all loopback addresses (any IP starting with `127.`).
- Whitelisting all addresses (any IP whatsoever.)
  - This part is quite fragile on MacOS and currently only works for about 10% of the versions.


### Example Usage
Below is an example CLI usage to create a revision 223 windows native client
that works for localhost, with the varp count being increased from 5,000 to
15,000, using the public RSA modulus shown below.

> [!NOTE]
> Clicking the green arrow below inside IntelliJ will fail as the command uses
> quotation marks which get translated to `&quot;`, breaking the command itself.
> Copy the command below and run it from a CLI manually!

Below is an example CLI command to create a patched client. In this example,
we change the javconfig to our own provided URL, we change the worldlist url
that is used to initially boot up, we modify the varp count in the client from
5,000 to 15,000, we change any urls of `runescape.com` to `rsprox.net`, and lastly,
we rename any indication of `RuneScape`, `OldSchool RuneScape`, `oldschool` and
any other variation of it to ´RSProx`.

`./gradlew patch --args="-version=223.3 -type=win -javconfig=https://cdn.rsprox.net/example_javconfig.ws -worldlist=https://cdn.rsprox.net/example_worldlist.ws -varpcount=15000 -siteurl=rsprox.net -name=RSProx -modulus=b565bcfee5f4e6b71732c20dc755ac6e39a4e47ecee3baf31cc1f3e256e078b572daa57a5da23e46bd622557dd3676816ff66c72d93b1b0aee44e2e65190f3cdd957f5aaa8f07b7da2945e8cba2da1125a4d21a070f38f95e20c4fe8a31e4f8df25832a79127800b0a8c32c5d80270ab358157f4bbda9e42bff78568d751"`

#### Customisations

If the above preset isn't enough, the patcher can be directly customized in code
to achieve more changes. It currently supports constant string replacing,
byte pattern replacing (including wildcard support) as well as regex-based string
replacing. Outside of those, it is possible to write your own processor with special
logic, although at that point you're writing your own patcher effectively. The
example implementations can make it simpler, though.

## RuneLite Patcher

The RuneLite patcher is currently only available in an integrated form, strictly
only usable with RSProx itself. As such, for the time being, it is not possible
to use it for private servers without significantly changing the back-end code.
