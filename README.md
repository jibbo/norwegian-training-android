# 🚀 Norwy — Norwegian 4x4 Training Timer

A no-frills Android timer app for
the [Norwegian 4x4 interval training method](https://www.ntnu.edu/cerg/advice). Pick your fitness
level, start the timer, and let it guide you through intervals and rest periods.

Available on
the [Play Store](https://play.google.com/store/apps/details?id=com.github.jibbo.norwegiantraining).

---

<!-- SCREENSHOT PLACEHOLDER -->

| Screenshot 1                                                                                                                      | Screenshot 2                                                                                                                      |
|-----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| ![](https://play-lh.googleusercontent.com/LLIrkm3ER-B8ocGcdiZQB21CCbQcC3h4qiVveW8tRfImGO2tWUMVjYJe3npUuB43qM78eKK8AyatbzRAvBRjzA) | ![](https://play-lh.googleusercontent.com/k_wp-tu6hJyOIvixDo9rgwEbIIJV6PgcZvOITi4uLNDXGgqQYMYwA1bFnZR2BL_jIBEfENxbR2vnCPogTA8R8w) |
---

## Getting Started

**Build**

```bash
git clone https://github.com/jibbo/norwegian-training-android.git
cd norwegian-training-android
./gradlew assembleDebug
```

---

## Contributing

PRs are welcome. If it's a bug fix or small improvement, open one directly. For larger changes, open
an issue first so we're aligned before you invest time.

A few honest caveats:

- This is a solo side project — review times may be slow.
- I won't accept changes specific to alternative stores or mirrors.
- If your PR is merged, you'll be added to the credits screen.

---

## FAQ

**Why is the app paywalled?**
I've shipped free apps before and ended up drowning in support requests I couldn't sustain. The
paywall filters for users who have some skin in the game. A free trial is available, and the paywall
is easy to remove locally if you have the skills.

**Why is the free trial so limited?**
The current paywall is client-side, which makes it trivially bypassable. A server-side check would
complicate forking, which I don't want. So I kept the implementation simple and the trial limited —
feel free to remove it for personal use.

**Why not F-Droid?**
Maintaining multiple stores isn't something I have bandwidth for right now.

**Can I mirror this on another store?**
Yes, as long as you maintain it independently and I'm not on the hook for support from non-Play
Store users.

---

## A Note on Code Quality

This is a personal project I build in my spare time with no intention of it being maintained by
anyone else. Architecture shortcuts were taken deliberately. If you're here to evaluate my
production work, this repo isn't the right sample.

---

## License

[GPL-3.0](LICENSE)