# Screenshot-led visual design

## Inspected references

The four available images were visually inspected at their actual paths:

| Reference | Implemented interpretation |
|---|---|
| `screenshots/login.png` | Teal medical-case icon, centered branding, left-aligned welcome, outlined fields, remember-session control, pill action and registration footer |
| `screenshots/register.png` | Left-aligned heading/subtitle, paired name fields when width and font size allow, outlined credentials and pill action |
| `screenshots/forgot_password.png` | Key tile, left-aligned heading, broad inputs/action, tinted information panel and return action |
| `screenshots/settings.png` | Surface app bar, uppercase section labels, rounded icon tiles, descriptive rows, separators and switches |

No profile, Home, medication, dose-detail or dark-mode reference images are present. Their
styles are derived from the same components and palette. The wireframe tooltip/frame in the
settings image is not application content.

## Tokens and components

- Petrol teal `#006B6B`, near-white background `#FAFDFD`, charcoal text `#191E1E`,
  muted supporting text `#4E6364`; corresponding dark surfaces and light teal accents.
- Existing platform sans typography, bold 28sp screen headings, 22sp compact headings,
  16sp semibold titles and 14–16sp body text, respecting user font scaling.
- Centralized dimensions, 8dp input corners, 12dp medium surfaces, 24dp cards/tiles,
  pill primary actions with a 56dp minimum height.
- Shared `AuthLayout`, `SectionIntro`, `BrandIcon`, `AppTextField`, `PasswordField`,
  `PrimaryActionButton` and `InformationPanel` in `component/DesignComponents.kt`.
- Auth content scrolls with the keyboard and is centered with a 560dp maximum width.
  Name fields share a row only with at least 280dp of content width and font scale ≤1.2.
  Other sizes stack them. Login session/recovery/footer content can wrap.
- The scaffold owns system insets; content consumes its padding. App bars use route titles,
  auth screens own their heading, and medication/dose pages have no duplicate title.
- Android system bar icon contrast follows the app's chosen theme.

## Functional adaptations

The screenshots are visual references, not evidence of implemented services:

- Recovery keeps the local-code flow, password confirmation, validation and single-use code
  behavior. It never promises a recovery email.
- Login has no Google/Apple actions because no such authentication providers are connected.
- Registration keeps the recovery-code guidance. No fictional terms/privacy acceptance or
  external legal links are inserted.
- Settings uses the actual version state, not the screenshot's sample version. Security
  leads to Profile's working recovery-code flow. Privacy opens factual local-data information;
  it is not presented as a legally reviewed privacy policy.
- The current bottom bar has Home/Scan/Profile/Settings, matching the master document now
  that Scan has a real implementation. The image's Cabinet/Doses destinations are not copied;
  stock and pending doses are reachable from Home. No nonfunctional destinations are added.
- The original splash logo is preserved. Native Material vector icons supply the reference's
  medical/key imagery; the redesign itself adds no image assets or dependencies. The later
  scanner block adds the separately documented Google SDK.

## Validation and review

Checked: resource XML parses, localized resource keys match, `git diff --check` passes.
Calculated static contrast ratios for the principal enabled text combinations:

| Combination | Ratio |
|---|---:|
| Light primary button | 6.33:1 |
| Light body / background | 16.48:1 |
| Light secondary text / background | 6.24:1 |
| Light information panel | 8.35:1 |
| Dark primary button | 7.71:1 |
| Dark body / background | 14.72:1 |
| Dark secondary text / background | 10.55:1 |

These values do not constitute an audit of every rendered state or a compliance claim.
No domain or persistence behavior was changed for this redesign; no new visual unit tests
were added. Full compilation, rendered comparison and native accessibility checks remain
pending in this environment.

Open `presentation/preview/DesignPreviews.kt` in Android Studio after resource generation.
It provides Login, Register, Recovery and Settings previews, including dark mode and a
320dp registration preview with 1.5× text. Preview definitions were added; no rendered
preview images are claimed here.

Manual review on Android and iOS:

1. Compare the four screens with the references at equivalent logical width. Check the
   derived Home, Profile, medication and dose detail for consistent typography and actions.
2. Use 320dp and 390dp widths, landscape, keyboard open, and 1.5×/2× text. Scroll to all
   fields and actions; check the paired name-field breakpoint and wrapping login controls.
3. Switch theme from Settings. Check system bars, fields, enabled/disabled/loading actions,
   errors, dialogs, cards and bottom navigation in both modes.
4. With TalkBack/VoiceOver, verify field labels, password toggles, whole-row checkbox/switch
   actions, headings, error announcements and predictable Back navigation.
5. Verify login, registration and local-code recovery; ensure loading preserves the form.
   Check Settings → Profile, Security, Privacy, About and logout. Test both Spanish and English.
6. Recheck medication create/edit and dose confirm/skip through the restyled shared actions.
