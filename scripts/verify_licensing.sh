#!/usr/bin/env sh
set -eu

for file in LICENSE NOTICE NOTICE.md; do
  test -f "$file"
  git ls-files --error-unmatch "$file" >/dev/null
done

grep -Fq 'Apache License' LICENSE
grep -Fq 'Version 2.0, January 2004' LICENSE
grep -Fq 'END OF TERMS AND CONDITIONS' LICENSE

archive_files="$(git archive --format=tar HEAD | tar -tf -)"
printf '%s\n' "$archive_files" | grep -Fxq LICENSE
printf '%s\n' "$archive_files" | grep -Fxq NOTICE
printf '%s\n' "$archive_files" | grep -Fxq NOTICE.md
